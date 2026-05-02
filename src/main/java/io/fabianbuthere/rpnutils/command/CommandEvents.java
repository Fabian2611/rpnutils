package io.fabianbuthere.rpnutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.fabianbuthere.rpnutils.RPNUtilMod;
import io.fabianbuthere.rpnutils.data.PaymentSavedData;
import io.fabianbuthere.rpnutils.util.CutsceneHandler;
import io.github.lightman314.lightmanscurrency.common.data.CustomSaveData;
import io.github.lightman314.lightmanscurrency.common.data.types.TeamDataCache;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = RPNUtilMod.MOD_ID)
public class CommandEvents {
    private static ItemStack taxesBook() {
        final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"text\":\"Steuererklärung\"}");
        tag.put("display", displayTag);

        ListTag pages = new ListTag();

        pages.add(StringTag.valueOf("--Steuererklärung--\n\nName: Mustermann\n\nVorname: Max\n\nGeburtstag: 01.01.1800\n\nHauptwohnsitz: [IC]"));
        pages.add(StringTag.valueOf("---- Immobilien ----\n\n-- Immobilie 1 --\nArt: Wohnung/Haus\nAdresse: Straße 1\nGröße (m2): 100\n\n-- Immobilie 2 --\nArt: Wohnung/Haus\nAdresse: Straße 2\nGröße (m2): 100"));
        pages.add(StringTag.valueOf("---- Einkommen ----\n\n-- Beruf 1 --\nGehalt: 100$\nFirma: Musterfirma\nTätigkeit: Müllmann\n\n-- Beruf 2 --\nGehalt: 100$\nFirma: Muster GmbH\nTätigkeit: Müllfrau"));
        pages.add(StringTag.valueOf("---- Regeln ----\n\nSie sind verpflichtet ein mal im Monat eine Steuererklärung an das Büro des Finanzministeriums zu senden. Alle Angaben müssen wahrheitsgemäß sein. Steuerhinterziehung wird mit Haftstrafen geahndet. Das Finanzministerium"));
        pages.add(StringTag.valueOf("kann gelegentlich auch Kontrollen durchführen, ob alle Steuern korrekt abgerechnet werden. Dafür können Firmen überwacht und durchleuchtet werden. Auch die Größe von Wohnungen und Häusern werden überprüft."));

        tag.put("pages", pages);

        return book;
    }

    private static ItemStack kvgBook() {
        final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"text\":\"[KVG] - Vorlage\"}");
        tag.put("display", displayTag);

        ListTag pages = new ListTag();

        pages.add(StringTag.valueOf("-------------------\nErwerbsurkunde\n-------------------\n\nAdresse:\nMusterst. 1\n\nKäufer:\nMustermann, Max\n\nFläche (m²):\n100m²"));
        pages.add(StringTag.valueOf("-------------------\nErwerbsurkunde\n-------------------\n\nPreis:\n1000.00$\n\nUnterschrift Käufer:\n"));

        tag.put("pages", pages);

        return book;
    }

    private static ItemStack horseBook() {
        final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"text\":\"Antrag auf Pferdezulassung\"}");
        tag.put("display", displayTag);

        ListTag pages = new ListTag();

        pages.add(StringTag.valueOf("-------------------\nPferdezulassung\n-------------------\n\nName: Mustermann\n\nVorname: Max\n\nAdresse:\nMusterst. 1, [IC]"));
        pages.add(StringTag.valueOf("-------------------\nPferdezulassung\n-------------------\n\nFarbe: Braun\n\nFlecken: Weiß\n\nName: EinName [IC-01]"));

        tag.put("pages", pages);

        return book;
    }

    private static ItemStack weaponBook() {
        final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"text\":\"Antrag auf Waffenschein\"}");
        tag.put("display", displayTag);

        ListTag pages = new ListTag();

        pages.add(StringTag.valueOf("-------------------\nAntrag auf Waffenschein\n-------------------\n\nName: Mustermann\n\nVorname: Max\n\nGeboren: 01.01.1800\n\nAdresse:\nMusterst. 1, [IC]"));
        pages.add(StringTag.valueOf("-------------------\nAntrag auf Waffenschein\n-------------------\n\nGrund:\nSehr guter Grund wieso ich einen Waffenschein bekommen sollte.\n\nUnterschrift:\n_______________"));

        tag.put("pages", pages);

        return book;
    }

    private static ItemStack importBook() {
        final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"text\":\"Importantrag\"}");
        tag.put("display", displayTag);

        ListTag pages = new ListTag();

        pages.add(StringTag.valueOf("-------------------\nImportantrag\n-------------------\n\nName: Mustermann\n\nVorname: Max\n\nGeboren: 01.01.1800\n\nAdresse:\nMusterst. 1, [IC]"));
        pages.add(StringTag.valueOf("-------------------\nImportantrag\n-------------------\n\nWaren:\n\n"));
        pages.add(StringTag.valueOf("-------------------\nImportantrag\n-------------------\n\nWaren:\n\n"));
        pages.add(StringTag.valueOf("-------------------\nImportantrag\n-------------------\n\n*wenn notwendig weitere Seiten erstellen*"));

        tag.put("pages", pages);

        return book;
    }

    private static ItemStack companyBook() {
        final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"text\":\"Antrag auf Firmenlizenz\"}");
        tag.put("display", displayTag);

        ListTag pages = new ListTag();

        pages.add(StringTag.valueOf("-------------------\nBesitzerangaben\n-------------------\n\nName: Mustermann\n\nVorname: Max\n\nGeboren: 01.01.1800\n\nAdresse:\nMusterst. 1, [IC]"));
        pages.add(StringTag.valueOf("-------------------\nFirmenangaben\n-------------------\n\nName: Muster GmbH\n\nArt: Produktion\n\nUnfallhaftung: Ja"));
        pages.add(StringTag.valueOf("-------------------\nFirmenstandorte\n-------------------\n\n- Musterst. 1, [IC]\n- Neustr. 2, [SC]"));

        tag.put("pages", pages);

        return book;
    }

    private static ItemStack companyTaxBook() {
        final ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"text\":\"Steueranhang Firmen\"}");
        tag.put("display", displayTag);

        ListTag pages = new ListTag();

        pages.add(StringTag.valueOf("--Firmensteuer--\n\nName: Mustermann\n\nVorname: Max\n\nGeboren: 01.01.1800"));
        pages.add(StringTag.valueOf("---- Umsatz ----\n\n-- Firma 1 --\nName: Muster GmbH\nSektor: Produktion\nAdresse: Testst. 1\nUmsatz: 100.00$\n\n-- Firma 2 --\nName: Beispiel AG\nSektor: Handel\nAdresse: Beispielst. 2\nUmsatz: 200.00$"));
        pages.add(StringTag.valueOf("---- Ausgaben ----\n\n-- Muster GmbH --\nBetrag: 100.00$\nGrund: Baumaterial\n\n-- Beispiel AG --\nBetrag: 200.00$\nGrund: Waren\n\n-- Beispiel AG --\nBetrag: 50.00$\nGrund: Werbung"));

        tag.put("pages", pages);

        return book;
    }

    private static int signDocument(ServerPlayer player, String stamp, String name, String requiredTag, String authority, String state) {
        if (!player.getTags().contains(requiredTag)) {
            player.sendSystemMessage(Component.literal("Du hast keine Berechtigung um dieses Dokument zu signieren."));
            return 0;
        }

        ItemStack book = player.getMainHandItem();
        if (!book.getItem().equals(Items.WRITABLE_BOOK)) {
            player.sendSystemMessage(Component.literal("Du musst ein unsigniertes Buch in der Hand halten um es zu signieren."));
            return 0;
        }
        if (stamp.contains("[" ) || stamp.contains("]")) {
            player.sendSystemMessage(Component.literal("Der Stempel darf keine eckigen Klammern enthalten."));
            return 0;
        }

        CompoundTag tag = book.getOrCreateTag().copy();
        ListTag oldPages = tag.contains("pages", 9) ? tag.getList("pages", 8) : new ListTag();

        ItemStack signed = new ItemStack(Items.WRITTEN_BOOK);

        CompoundTag newTag = new CompoundTag();
        newTag.putString("title", "[%s] %s".formatted(stamp, name));
        newTag.putString("author", authority);

        CompoundTag displayTag = new CompoundTag();
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf("{\"text\":\"[%s] | %s\",\"color\":\"dark_purple\",\"italic\":false}".formatted(stamp, state)));
        displayTag.put("Lore", lore);
        newTag.put("display", displayTag);

        ListTag newPages = new ListTag();

        for (int i = 0; i < oldPages.size(); i++) {
            String pageContent = oldPages.getString(i);
            // Escape special characters (quotes and backslashes)
            pageContent = pageContent.replace("\\", "\\\\").replace("\"", "\\\"");
            // Replace newlines with the escaped newline sequence
            pageContent = pageContent.replace("\n", "\\n");
            // Create a JSON text component
            String jsonPage = "{\"text\":\"" + pageContent + "\"}";
            newPages.add(StringTag.valueOf(jsonPage));
        }

        newTag.put("pages", newPages);
        signed.setTag(newTag);

        player.addItem(signed);
        return 1;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("doc")
                        .then(Commands.literal("steuer")
                                .executes(context -> {
                                    context.getSource().getPlayerOrException().addItem(taxesBook());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("pferd")
                                .executes(context -> {
                                    context.getSource().getPlayerOrException().addItem(horseBook());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("waffen")
                                .executes(context -> {
                                    context.getSource().getPlayerOrException().addItem(weaponBook());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("import")
                                .executes(context -> {
                                    context.getSource().getPlayerOrException().addItem(importBook());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("firma")
                                .executes(context -> {
                                    context.getSource().getPlayerOrException().addItem(companyBook());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("steuer_firma")
                                .executes(context -> {
                                    context.getSource().getPlayerOrException().addItem(companyTaxBook());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("kvg")
                                .executes(context -> {
                                    context.getSource().getPlayerOrException().addItem(kvgBook());
                                    return 1;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("sign")
                        .then(Commands.argument("stamp", StringArgumentType.string())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            return signDocument(
                                                    context.getSource().getPlayerOrException(),
                                                    StringArgumentType.getString(context, "stamp"),
                                                    StringArgumentType.getString(context, "name"),
                                                    "rpn.sign_documents",
                                                    "Rathaus [IC]",
                                                    "Infinity City"
                                            );
                                        })
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("sign_isa")
                        .then(Commands.argument("stamp", StringArgumentType.string())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> signDocument(
                                                    context.getSource().getPlayerOrException(),
                                                    StringArgumentType.getString(context, "stamp"),
                                                    StringArgumentType.getString(context, "name"),
                                                    "rpn.isa",
                                                    "Infinity State Agency",
                                                    "ISA"
                                            )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("sign_police")
                        .then(Commands.argument("stamp", StringArgumentType.string())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context ->
                                                signDocument(
                                                        context.getSource().getPlayerOrException(),
                                                        StringArgumentType.getString(context, "stamp"),
                                                        StringArgumentType.getString(context, "name"),
                                                        "rpn.sign_documents_police",
                                                        "Polizei [IC]",
                                                        "Infinity City"
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("sign_bank")
                        .then(Commands.argument("stamp", StringArgumentType.string())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context ->
                                                signDocument(
                                                        context.getSource().getPlayerOrException(),
                                                        StringArgumentType.getString(context, "stamp"),
                                                        StringArgumentType.getString(context, "name"),
                                                        "rpn.sign_documents_bank",
                                                        "Staatsbank [IC]",
                                                        "Infinity City"
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("sign_savanna")
                        .then(Commands.argument("stamp", StringArgumentType.string())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context ->
                                                signDocument(
                                                        context.getSource().getPlayerOrException(),
                                                        StringArgumentType.getString(context, "stamp"),
                                                        StringArgumentType.getString(context, "name"),
                                                        "rpn.sign_documents_savanna",
                                                        "Rathaus [SC]",
                                                        "Savanna City"
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("sign_zwr")
                        .then(Commands.argument("stamp", StringArgumentType.string())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context ->
                                                signDocument(
                                                        context.getSource().getPlayerOrException(),
                                                        StringArgumentType.getString(context, "stamp"),
                                                        StringArgumentType.getString(context, "name"),
                                                        "rpn.zwr",
                                                        "Zentraler Wirtschaftsrat",
                                                        "ZWR"
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("serial")
                        .then(Commands.argument("serial", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    if (!player.getTags().contains("rpn.use_serial")) {
                                        context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Du hast keine Berechtigung um Seriennummern zu vergeben."));
                                        return 0;
                                    }
                                    ItemStack item = player.getMainHandItem();
                                    if (item.isEmpty() || item.getItem().equals(Items.AIR)) {
                                        context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Du musst ein Item in der Hand halten um ihm eine Seriennummer zu geben."));
                                        return 0;
                                    }
                                    CompoundTag tag = item.getOrCreateTag();
                                    CompoundTag displayTag = tag.contains("display", 10) ? tag.getCompound("display").copy() : new CompoundTag();
                                    ListTag lore = new ListTag();

                                    String serialInput = StringArgumentType.getString(context, "serial");
                                    Component loreComponent = Component.literal("[%s]".formatted(serialInput));
                                    String loreJson = Component.Serializer.toJson(loreComponent);

                                    lore.add(StringTag.valueOf(loreJson));
                                    displayTag.put("Lore", lore);
                                    tag.put("display", displayTag);
                                    item.setTag(tag);

                                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                                    return 1;
                                })
                        )
        );

        // New commands from KubeJS migration
        dispatcher.register(
                Commands.literal("rpndbg")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("forceperso")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), "tag " + player.getScoreboardName() + " remove rpn.perso");
                                    context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), "perso " + player.getScoreboardName());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("unperso")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), "tag " + player.getScoreboardName() + " remove rpn.perso");
                                    return 1;
                                })
                                .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "player");
                                            context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), "tag " + player.getScoreboardName() + " remove rpn.perso");
                                            return 1;
                                        })
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("isa_tell")
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
                                            if (!sourcePlayer.getTags().contains("rpn.isa")) {
                                                sourcePlayer.sendSystemMessage(Component.literal("You do not have permission to use this command."));
                                                return 0;
                                            }
                                            ServerPlayer targetPlayer = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "player");
                                            String message = StringArgumentType.getString(context, "message");
                                            
                                            String tellrawJson = String.format("[{\"text\":\"<ISA Agent> \",\"color\":\"red\"},{\"text\":\"%s\", \"color\":\"white\"}]", message);
                                            
                                            context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource().withSuppressedOutput().withPermission(4), 
                                                    "tellraw " + targetPlayer.getScoreboardName() + " " + tellrawJson);
                                            
                                            context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource().withSuppressedOutput().withPermission(4), 
                                                    "tellraw @a[tag=rpn.isa,name=!" + targetPlayer.getScoreboardName() + "] " + tellrawJson);
                                            return 1;
                                        })
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("perso")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "player");
                                    io.fabianbuthere.rpnutils.util.PersonalausweisHandler.startPersonalausweisCreation(player);
                                    return 1;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("rathaus")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("ring")
                                .executes(context -> {
                                    for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                                        context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource().withSuppressedOutput().withPermission(4), 
                                                "playsound minecraft:entity.experience_orb.pickup master " + player.getScoreboardName() + "[tag=rpn.ring] ~ ~ ~ 1 1");
                                        context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource().withSuppressedOutput().withPermission(4), 
                                                "tellraw " + player.getScoreboardName() + "[tag=rpn.ring] {\"text\":\"Im Rathaus wurde geklingelt!\",\"color\":\"green\",\"bold\":true,\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"" + context.getSource().getTextName() + "\"}]}}");
                                    }
                                    return 1;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("cutscene")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("index", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                        .executes(context -> {
                                            int index = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "index");
                                            ServerPlayer player = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "player");
                                            
                                            if (index < 0 || index >= CutsceneHandler.getGoalsCount()) {
                                                player.sendSystemMessage(Component.literal("Invalid cutscene index, stopping quests."));
                                            }
                                            
                                            CutsceneHandler.startCutscene(player, index);
                                            return 1;
                                        })
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("discord")
                        .executes(context -> {
                            context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Hier ist der Link zum Discord: https://discord.gg/invite/JRQdV5BPjY").withStyle(net.minecraft.ChatFormatting.GREEN));
                            return 1;
                        })
        );

        dispatcher.register(
                Commands.literal("change_perso")
                        .requires(source -> source.isPlayer() &&
                                source.hasPermission(2) ||
                                (source.getPlayer().getTags().contains("rpn.modify_perso") &&
                                source.getPlayer().getInventory().getSelected().getItem() == Items.WRITTEN_BOOK)
                        )
                        .then(Commands.literal("waffenschein")
                                .executes(ctx -> {
                                    var pages = ctx.getSource().getPlayerOrException().getInventory().getSelected().getOrCreateTag().get("pages");
                                    if (pages == null) {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand ist kein Buch!"));
                                        return 0;
                                    }
                                    if (pages instanceof ListTag listTag) {
                                        listTag.set(0, StringTag.valueOf(listTag.getString(0).replace("Waffenschein: ✖", "Waffenschein: ✔")));
                                        return 1;
                                    } else {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand hat kein gültiges Format!"));
                                        return 0;
                                    }
                                })
                        )
                        .then(Commands.literal("flugschein")
                        .executes(ctx -> {
                            var pages = ctx.getSource().getPlayerOrException().getInventory().getSelected().getOrCreateTag().get("pages");
                            if (pages == null) {
                                ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand ist kein Buch!"));
                                return 0;
                            }
                            if (pages instanceof ListTag listTag) {
                                listTag.set(0, StringTag.valueOf(listTag.getString(0).replace("Flugschein: ✖", "Flugschein: ✔")));
                                return 1;
                            } else {
                                ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand hat kein gültiges Format!"));
                                return 0;
                            }
                                })
                        )
                        .then(Commands.literal("fuehrerschein")
                                .executes(ctx -> {
                                    var pages = ctx.getSource().getPlayerOrException().getInventory().getSelected().getOrCreateTag().get("pages");
                                    if (pages == null) {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand ist kein Buch!"));
                                        return 0;
                                    }
                                    if (pages instanceof ListTag listTag) {
                                        listTag.set(0, StringTag.valueOf(listTag.getString(0).replace("Führerschein: ✖", "Führerschein: ✔")));
                                        return 1;
                                    } else {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand hat kein gültiges Format!"));
                                        return 0;
                                    }
                                })
                        )
                        .then(Commands.literal("anwaltslizenz")
                                .executes(ctx -> {
                                    var pages = ctx.getSource().getPlayerOrException().getInventory().getSelected().getOrCreateTag().get("pages");
                                    if (pages == null) {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand ist kein Buch!"));
                                        return 0;
                                    }
                                    if (pages instanceof ListTag listTag) {
                                        listTag.set(0, StringTag.valueOf(listTag.getString(0).replace("Anwaltslizenz: ✖", "Anwaltslizenz: ✔")));
                                        return 1;
                                    } else {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand hat kein gültiges Format!"));
                                        return 0;
                                    }
                                })
                        )
                        .then(Commands.literal("arztlizenz")
                                .executes(ctx -> {
                                    var pages = ctx.getSource().getPlayerOrException().getInventory().getSelected().getOrCreateTag().get("pages");
                                    if (pages == null) {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand ist kein Buch!"));
                                        return 0;
                                    }
                                    if (pages instanceof ListTag listTag) {
                                        listTag.set(0, StringTag.valueOf(listTag.getString(0).replace("Arztlizenz: ✖", "Arztlizenz: ✔")));
                                        return 1;
                                    } else {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Das Item in deiner Hand hat kein gültiges Format!"));
                                        return 0;
                                    }
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("payment")
                        .requires(CommandSourceStack::isPlayer)
                        .then(
                                Commands.literal("add")
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                    .then(Commands.argument("days", IntegerArgumentType.integer(0))
                                                            .then(Commands.argument("account", StringArgumentType.greedyString())
                                                                    .suggests((ctx, builder) -> {
                                                                        CustomSaveData.getData(TeamDataCache.TYPE).getAllTeams().forEach(team -> {
                                                                            if (team.hasBankAccount()) {
                                                                                builder.suggest(team.getName());
                                                                            }
                                                                        });
                                                                        return builder.buildFuture();
                                                                    })
                                                                    .executes(ctx -> {
                                                                        var data = PaymentSavedData.get(ctx.getSource().getLevel());
                                                                        var player = ctx.getSource().getPlayerOrException();

                                                                        var acc = ctx.getArgument("account", String.class);
                                                                        if (CustomSaveData.getData(TeamDataCache.TYPE).getAllTeams().stream().noneMatch(t -> t.getName().equalsIgnoreCase(acc))) {
                                                                            ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Empfänger ungültig!").withStyle(ChatFormatting.RED));
                                                                            return 0;
                                                                        }

                                                                        data.addPayment(player.getUUID(), new PaymentSavedData.PaymentData(
                                                                                ctx.getArgument("amount", Integer.class) * 100,
                                                                                ctx.getArgument("days", Integer.class),
                                                                                System.currentTimeMillis(),
                                                                                0,
                                                                                ctx.getArgument("account", String.class)
                                                                        ));

                                                                        player.sendSystemMessage(Component.literal("Zahlung hinzugefügt!").withStyle(ChatFormatting.GREEN));
                                                                        return 1;
                                                                    })
                                                            )
                                                    )
                                            )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(Commands.argument("account", StringArgumentType.greedyString())
                                                .suggests((ctx, builder) -> {
                                                    PaymentSavedData.get(ctx.getSource().getLevel()).getOrCreatePayments(ctx.getSource().getPlayerOrException().getUUID()).forEach(payment -> {
                                                        builder.suggest(payment.receiver());
                                                    });
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    var data = PaymentSavedData.get(ctx.getSource().getLevel());
                                                    var player = ctx.getSource().getPlayerOrException();
                                                    String account = ctx.getArgument("account", String.class);

                                                    if (data.removePayment(player.getUUID(), account)) {
                                                        player.sendSystemMessage(Component.literal("Zahlung an " + account + " entfernt."));
                                                        return 1;
                                                    } else {
                                                        player.sendSystemMessage(Component.literal("An diesen Empfänger gibt es keine Zahlung.").withStyle(ChatFormatting.RED));
                                                        return 0;
                                                    }
                                                })
                                        )
                        )
        );
    }
}
