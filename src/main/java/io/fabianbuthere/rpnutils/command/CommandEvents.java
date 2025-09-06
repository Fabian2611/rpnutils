package io.fabianbuthere.rpnutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.fabianbuthere.rpnutils.RPNUtilMod;
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
        newTag.putString("title", name);
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
    }
}
