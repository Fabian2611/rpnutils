package io.fabianbuthere.rpnutils.util;

import io.fabianbuthere.rpnutils.RPNUtilMod;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersonalausweisHandler {
    private static final Map<UUID, PlayerData> playerInputs = new HashMap<>();
    private static final int COPY_X = -503;
    private static final int COPY_Y = 74;
    private static final int COPY_Z = 91;

    private static class PlayerData {
        int step = 0;
        String[] data = new String[]{"", "", "", "", ""};
    }

    public static void startPersonalausweisCreation(ServerPlayer player) {
        if (player.getTags().contains("rpn.perso")) {
            player.sendSystemMessage(Component.literal("Sie haben schon einen Personalausweis!").withStyle(style -> style.withColor(0xFFFF55))); // Yellow
            return;
        }
        player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "tag " + player.getScoreboardName() + " add rpn.perso");
        player.sendSystemMessage(Component.literal("Willkommen im Rathaus! Bitte gib deine Daten ein.").withStyle(style -> style.withColor(0xFFFF55)));
        player.sendSystemMessage(Component.literal("Das wird deine Identität für den Rest des Servers sein, also denk gut darüber nach.").withStyle(style -> style.withColor(0xFFFF55)));
        askForName(player);
    }

    private static void askForName(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("Vorname (max. 11 Buchstaben):"));
        playerInputs.put(player.getUUID(), new PlayerData());
    }

    public static boolean handleInput(ServerPlayer player, String input) {
        if (!playerInputs.containsKey(player.getUUID())) {
            return false;
        }

        PlayerData playerData = playerInputs.get(player.getUUID());

        switch (playerData.step) {
            case 0: // First name
                if (input.length() > 11) {
                    player.sendSystemMessage(Component.literal("Der Vorname ist zu lang!"));
                    return true;
                }
                playerData.data[1] = StringUtils.title(input);
                player.sendSystemMessage(Component.literal("Nachname (max. 10 Buchstaben):"));
                break;
            case 1: // Last name
                if (input.length() > 10) {
                    player.sendSystemMessage(Component.literal("Der Nachname ist zu lang!"));
                    return true;
                }
                playerData.data[2] = StringUtils.title(input);
                player.sendSystemMessage(Component.literal("Bitte gib deinen Geburtstag ein (z.B. 01.01.1840):"));
                break;
            case 2: // Birthday
                if (!input.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
                    player.sendSystemMessage(Component.literal("Ungültiges Format für den Geburtstag!"));
                    return true;
                }

                String[] dateParts = input.split("\\.");
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[2]);
                if (day < 1 || day > 31 || month < 1 || month > 12 || year >= 1863) {
                    player.sendSystemMessage(Component.literal("Ungültiges Geburtsdatum! Denk daran, dass wir uns im Jahr 1870 befinden. Du solltest außerdem (im RP) über 18 Jahre alt sein."));
                    return true;
                }
                playerData.data[3] = input;
                playerData.data[4] = "[IC]";
                player.sendSystemMessage(Component.literal("Personalausweis wird erstellt...").withStyle(style -> style.withColor(0xFFFF55)));
                player.addTag("rpn.continuescene");
                givePersonalausweis(player, playerData.data);
                playerInputs.remove(player.getUUID());
                playerData.step++;
                return true;
        }

        playerData.step++;
        return true;
    }

    private static void givePersonalausweis(ServerPlayer player, String[] data) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("author", "[IC] Rathaus");
        tag.putString("title", "[PA] " + data[1] + " " + data[2]);

        CompoundTag displayTag = new CompoundTag();
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf("{\"text\":\"[PA] | Infinity City\",\"color\":\"dark_purple\",\"italic\":\"false\"}"));
        displayTag.put("Lore", lore);
        tag.put("display", displayTag);

        ListTag pages = new ListTag();
        String pageContent = "-------------------\n" +
                "Personalausweis\n" +
                "-------------------\n" +
                "Vorname: " + data[1] + "\n" +
                "Nachname: " + data[2] + "\n" +
                "Geburtstag: " + data[3] + "\n" +
                "-------------------\n" +
                "Lizenzen:\n" +
                "Waffenschein: ✖\n" +
                "Flugschein: ✖\n" +
                "Führerschein: ✖\n" +
                "Anwaltslizenz: ✖\n" +
                "Arztlizenz: ✖\n";

        String jsonPage = "{\"text\":\"" + pageContent.replace("\n", "\\n") + "\"}";
        pages.add(StringTag.valueOf(jsonPage));
        tag.put("pages", pages);

        player.addItem(book.copy());

        BlockPos barrelPos = new BlockPos(COPY_X, COPY_Y, COPY_Z);
        ServerLevel serverLevel = player.serverLevel();

        serverLevel.getServer().execute(() -> {
            BlockEntity blockEntity = serverLevel.getBlockEntity(barrelPos);

            if (blockEntity instanceof BarrelBlockEntity bbe) {
                bbe.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                    ItemStack stackToInsert = book.copy();

                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        stackToInsert = itemHandler.insertItem(i, stackToInsert, false);
                        if (stackToInsert.isEmpty()) {
                            break;
                        }
                    }
                });

                bbe.setChanged();
                serverLevel.sendBlockUpdated(barrelPos, bbe.getBlockState(), bbe.getBlockState(), 3);
            } else {
                RPNUtilMod.LOGGER.warn("BarrelBlockEntity not found!");
            }
        });

        Commands cmd = player.getServer().getCommands();

        if (player.getTags().contains("rpn.continuescene")) {
            giveItem(player, "lightmanscurrency", "wallet_leather", 1);
            giveItem(player, "lightmanscurrency", "coin_diamond", 5);
            giveItem(player, "minecraft", "apple", 32);

            cmd.performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "tag " + player.getScoreboardName() + " remove rpn.continuescene");
            cmd.performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "cutscene 1 " + player.getScoreboardName());
            cmd.performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "clear " + player.getScoreboardName() + " patchouli:guide_book");
        }
    }

    @SuppressWarnings("removal")
    private static void giveItem(ServerPlayer player, String namespace, String itemId, int count) {
        try {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.of(namespace + ":" + itemId, ':'));
            if (item != null) {
                player.addItem(new ItemStack(item, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
