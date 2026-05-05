package io.fabianbuthere.rpnutils.data;

import io.fabianbuthere.rpnutils.RPNUtilMod;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.data.CustomSaveData;
import io.github.lightman314.lightmanscurrency.common.data.types.TeamDataCache;
import io.github.lightman314.lightmanscurrency.common.impl.BankAPIImpl;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.LowBalanceNotification;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class PaymentSavedData extends SavedData {
    public record PaymentData(int amount, int days_interval, long started_at, long last_paid, String receiver) {
        public boolean isDue() {
            long now = System.currentTimeMillis();
            long intervalMs = (long) days_interval * 24 * 60 * 60 * 1000;

            return last_paid == 0 || (now - last_paid >= intervalMs);
        }
    }

    private final Map<UUID, List<PaymentData>> playerPayments = new HashMap<>();

    public PaymentSavedData() {}

    public static PaymentSavedData load(CompoundTag nbt) {
        PaymentSavedData data = new PaymentSavedData();
        CompoundTag allPlayersNbt = nbt.getCompound("all_player_data");

        for (String uuidString : allPlayersNbt.getAllKeys()) {
            UUID uuid = UUID.fromString(uuidString);
            ListTag listNbt = allPlayersNbt.getList(uuidString, Tag.TAG_COMPOUND);
            List<PaymentData> payments = new ArrayList<>();

            for (int i = 0; i < listNbt.size(); i++) {
                CompoundTag entry = listNbt.getCompound(i);
                payments.add(new PaymentData(
                        entry.getInt("amount"),
                        entry.getInt("days_interval"),
                        entry.getLong("started_at"),
                        entry.getLong("last_paid"),
                        entry.getString("receiver")
                ));
            }
            data.playerPayments.put(uuid, payments);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag allPlayersNbt = new CompoundTag();

        playerPayments.forEach((uuid, list) -> {
            ListTag listNbt = new ListTag();
            for (PaymentData p : list) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("amount", p.amount());
                entry.putInt("days_interval", p.days_interval());
                entry.putLong("started_at", p.started_at());
                entry.putLong("last_paid", p.last_paid());
                entry.putString("receiver", p.receiver());
                listNbt.add(entry);
            }
            allPlayersNbt.put(uuid.toString(), listNbt);
        });

        nbt.put("all_player_data", allPlayersNbt);
        return nbt;
    }

    public List<PaymentData> getOrCreatePayments(UUID playerUuid) {
        return playerPayments.computeIfAbsent(playerUuid, k -> new ArrayList<>());
    }

    public void addPayment(UUID playerUuid, PaymentData payment) {
        getOrCreatePayments(playerUuid).add(payment);
        this.setDirty();
    }

    public static PaymentSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(PaymentSavedData::load, PaymentSavedData::new, "rpn_player_payments");
    }

    public boolean removePayment(UUID playerUuid, String receiverName) {
        List<PaymentData> list = playerPayments.get(playerUuid);
        if (list != null) {
            boolean removed = list.removeIf(p -> p.receiver().equals(receiverName));
            if (removed) {
                this.setDirty();
                return true;
            }
        }
        return false;
    }

    public void tick(ServerLevel level) {
        long now = System.currentTimeMillis();

        // DEBUG: Check if data even exists
        int totalPlayers = playerPayments.size();
        RPNUtilMod.LOGGER.debug("[RPN DEBUG] Tick started. Map size: {}", totalPlayers);

        if (totalPlayers == 0) {
            return;
        }

        boolean saveRequired = false;
        // Use a copy of the keys to prevent ConcurrentModificationException
        List<UUID> uuids = new ArrayList<>(playerPayments.keySet());

        for (UUID playerUuid : uuids) {
            RPNUtilMod.LOGGER.debug("[RPN DEBUG] Processing Player: {}", playerUuid);

            List<PaymentData> payments = playerPayments.get(playerUuid);
            if (payments == null || payments.isEmpty()) {
                RPNUtilMod.LOGGER.debug("[RPN DEBUG] No payments found for player {}", playerUuid);
                continue;
            }

            List<PaymentData> nextTickList = new ArrayList<>();
            boolean playerChanged = false;

            for (PaymentData p : payments) {
                RPNUtilMod.LOGGER.debug("[RPN DEBUG] Checking payment to: {}. Is Due: {}", p.receiver(), p.isDue());

                if (p.isDue()) {
                    // 1. Locate Sender
                    BankReference senderRef = PlayerBankReference.of(playerUuid);
                    IBankAccount senderAcc = senderRef.get();
                    if (senderAcc == null) {
                        RPNUtilMod.LOGGER.warn("[RPN DEBUG] Could not find Bank Account for UUID: {}", playerUuid);
                        nextTickList.add(p);
                        continue;
                    }

                    // 2. Locate Receiver
                    String recName = p.receiver() + "'s Bank Account";
                    var receiverAccOpt = BankAPIImpl.INSTANCE.GetAllBankAccounts(false).stream()
                            .filter(t -> t.getName().getString().equalsIgnoreCase(recName))
                            .findAny();

                    if (receiverAccOpt.isEmpty()) {
                        RPNUtilMod.LOGGER.warn("[RPN DEBUG] Receiver account not found: {}", recName);
                        nextTickList.add(p);
                        continue;
                    }

                    IBankAccount receiverAcc = receiverAccOpt.get();
                    MoneyValue amountToPay = CoinValue.fromNumber("main", p.amount());

                    RPNUtilMod.LOGGER.debug("[RPN DEBUG] Attempting payment. Amount: {}. Sender Balance: {}",
                            amountToPay.getText().getString(),
                            senderAcc.getMoneyStorage().getStoredMoney().getString());

                    if (!amountToPay.isEmpty() && senderAcc.getMoneyStorage().containsValue(amountToPay)) {
                        var withdrawResult = BankAPIImpl.API.BankWithdrawFromServer(senderAcc, amountToPay, false);

                        if (withdrawResult.getFirst()) {
                            boolean success = BankAPIImpl.API.BankDepositFromServer(receiverAcc, withdrawResult.getSecond(), false);
                            if (success) {
                                RPNUtilMod.LOGGER.debug("[RPN DEBUG] SUCCESS! Payment processed for {}", playerUuid);
                                nextTickList.add(new PaymentData(p.amount(), p.days_interval(), p.started_at(), now, p.receiver()));
                                playerChanged = true;
                                saveRequired = true;
                                continue;
                            } else {
                                RPNUtilMod.LOGGER.error("[RPN DEBUG] Deposit failed! Refunding sender.");
                                BankAPIImpl.API.BankDepositFromServer(senderAcc, withdrawResult.getSecond(), false);
                            }
                        } else {
                            RPNUtilMod.LOGGER.error("[RPN DEBUG] Withdrawal failed for unknown reason.");
                        }
                    } else {
                        RPNUtilMod.LOGGER.warn("[RPN DEBUG] Insufficient funds or invalid currency ID.");
                        senderAcc.pushLocalNotification(LowBalanceNotification.create(senderAcc.getName(), amountToPay).get());
                    }
                }

                nextTickList.add(p);
            }

            if (playerChanged) {
                playerPayments.put(playerUuid, nextTickList);
            }
        }

        if (saveRequired) {
            RPNUtilMod.LOGGER.debug("[RPN DEBUG] Saving changes to disk.");
            this.setDirty();
        }
    }
}
