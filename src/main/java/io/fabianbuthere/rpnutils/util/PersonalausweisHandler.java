package io.fabianbuthere.rpnutils.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                playerData.data[1] = input;
                player.sendSystemMessage(Component.literal("Nachname (max. 10 Buchstaben):"));
                break;
            case 1: // Last name
                if (input.length() > 10) {
                    player.sendSystemMessage(Component.literal("Der Nachname ist zu lang!"));
                    return true;
                }
                playerData.data[2] = input;
                player.sendSystemMessage(Component.literal("Bitte gib deinen Geburtstag ein (DD.MM.YYYY):"));
                break;
            case 2: // Birthday
                if (!input.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
                    player.sendSystemMessage(Component.literal("Ungültiges Format für den Geburtstag!"));
                    return true;
                }
                playerData.data[3] = input;
                player.sendSystemMessage(Component.literal("Hauptwohnsitz ([IC] eingeben): "));
                break;
            case 3: // Residence
                if (!input.equals("[IC]")) {
                    player.sendSystemMessage(Component.literal("Bitte lies die Anweisungen."));
                    return true;
                }
                playerData.data[4] = input;
                player.sendSystemMessage(Component.literal("Personalausweis wird erstellt...").withStyle(style -> style.withColor(0xFFFF55)));
                givePersonalausweis(player, playerData.data);
                playerInputs.remove(player.getUUID());
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
                "Hauptwohnsitz: " + data[4] + "\n" +
                "-------------------\n" +
                "Lizenzen:\n" +
                "Waffenschein: ✖\n" +
                "Flugschein: ✖\n" +
                "Apothekerlizenz: ✖\n" +
                "Brauerlizenz: ✖\n";
        
        // JSON stringify logic similar to KubeJS
        String jsonPage = "{\"text\":\"" + pageContent.replace("\n", "\\n") + "\"}";
        pages.add(StringTag.valueOf(jsonPage));
        tag.put("pages", pages);

        player.addItem(book);

        // HACK: Just put the book into the barrel by command
        CompoundTag itemTag = book.save(new CompoundTag());
        String itemId = itemTag.getString("id");
        String tagString = itemTag.getCompound("tag").toString();
        
        String command = String.format("execute in minecraft:overworld run item replace block %d %d %d container.0 with %s%s", 
                COPY_X, COPY_Y, COPY_Z, itemId, tagString);
        
        player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), command);

        if (player.getTags().contains("rpn.continuescene")) {
            giveItem(player, "lightmanscurrency:wallet_leather", 1);
            giveItem(player, "lightmanscurrency:coin_diamond", 5);
            giveItem(player, "minecraft:apple", 16);
            
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "tag " + player.getScoreboardName() + " remove rpn.continuescene");
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "cutscene 1 " + player.getScoreboardName());
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "clear " + player.getScoreboardName() + " patchouli:guide_book");
        }
    }

    private static void giveItem(ServerPlayer player, String itemId, int count) {
        try {
            net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(itemId));
            if (item != null) {
                player.addItem(new ItemStack(item, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
