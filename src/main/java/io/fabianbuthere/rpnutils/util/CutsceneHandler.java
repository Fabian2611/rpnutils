package io.fabianbuthere.rpnutils.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Consumer;

public class CutsceneHandler {
    private static final Map<UUID, Integer> playerCutsceneIndex = new HashMap<>();

    private static class Goal {
        Vec3 location;
        List<String> pre;
        List<String> after;
        boolean next;
        Consumer<ServerPlayer> afterAction;
        Consumer<ServerPlayer> preAction;

        public Goal(Vec3 location, List<String> pre, List<String> after, boolean next, Consumer<ServerPlayer> preAction, Consumer<ServerPlayer> afterAction) {
            this.location = location;
            this.pre = pre;
            this.after = after;
            this.next = next;
            this.preAction = preAction;
            this.afterAction = afterAction;
        }
    }

    private static final List<Goal> goals = new ArrayList<>();

    static {
        // Goal 0
        goals.add(new Goal(
                new Vec3(-431, 72.5, 80.5),
                Arrays.asList(
                        "Bitte verlasse den Server nicht, während du im Tutorial bist.",
                        "Willkommen auf Roleplay.net!",
                        "Deine erste Aufgabe ist es, zum Rathaus zu gehen.",
                        "Folge einfach der grünen Linie."
                ),
                Collections.singletonList("Sprich jetzt mit Mr. RedBlood, um deinen Personalausweis zu bekommen."),
                false,
                player -> player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "doc tutorial"),
                PersonalausweisHandler::startPersonalausweisCreation
        ));

        // Goal 1
        goals.add(new Goal(
                new Vec3(-440.5, 67.5, 37.5),
                Arrays.asList(
                        "Gut gemacht!",
                        "Dir werden jetzt die wichtigsten Teile des Staats gezeigt.",
                        "Denk dran, an wichtigen Orten mit 'B' Waypoints zu setzen."
                ),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 2
        goals.add(new Goal(new Vec3(-494.5, 69.5, -33.5), Collections.emptyList(), Collections.emptyList(), true, null, null));
        // Goal 3
        goals.add(new Goal(new Vec3(-493.5, 68.5, -127.5), Collections.emptyList(), Collections.emptyList(), true, null, null));
        
        // Goal 4
        goals.add(new Goal(
                new Vec3(-599.5, 68.5, -126.5),
                Collections.singletonList("Das ist das Industriegebiet. Hier können Firmen Fabrikgrundstücke kaufen."),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 5
        goals.add(new Goal(new Vec3(-600.5, 68.5, -171), Collections.emptyList(), Collections.emptyList(), true, null, null));

        // Goal 6
        goals.add(new Goal(
                new Vec3(-691.5, 66.5, -156.5),
                Arrays.asList(
                        "Hier ist die Arztpraxis, gegenüber der Billigladen.",
                        "Er ist ziemlich günstig, hat dafür nicht immer Vorrat."
                ),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 7
        goals.add(new Goal(
                new Vec3(-385.5, 65.5, -132),
                Arrays.asList(
                        "Jetzt sind wir in Funkenbruch.",
                        "Hier gibt es günstige Wohnungen und einen Autohandel."
                ),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 8
        goals.add(new Goal(
                new Vec3(-237.5, 64.5, -131.5),
                Arrays.asList(
                        "Hier ist die Polizeistation.",
                        "Hier kannst du Anzeigen erstatten und deinen Führerschein machen.",
                        "Außerdem ist am Ende der Straße das Casino."
                ),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 9
        goals.add(new Goal(new Vec3(-202, 70.5, -56), Collections.emptyList(), Collections.emptyList(), true, null, null));

        // Goal 10
        goals.add(new Goal(
                new Vec3(-198, 68.5, 14),
                Arrays.asList(
                        "Willkommen in der Altstadt, dem Herzen von Infinity City.",
                        "Gerade befindest du dich vor der Bank."
                ),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 11
        goals.add(new Goal(
                new Vec3(-203.5, 67.5, 34.5),
                Arrays.asList(
                        "Das hier ist der Bioladen.",
                        "Hier bekommst du Essen und manche anderen hilfreichen Sachen."
                ),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 12
        goals.add(new Goal(
                new Vec3(-155, 70.5, -3.5),
                Arrays.asList(
                        "Hier ist die Schmiede.",
                        "Hier bekommst du Werkzeuge und Rüstung."
                ),
                Collections.emptyList(),
                true,
                null,
                null
        ));

        // Goal 13
        goals.add(new Goal(
                new Vec3(-141.5, 70.5, -55.5),
                Arrays.asList(
                        "Das ist die Baufirma.",
                        "Hier kannst du sowohl Materialien kaufen als auch Bauaufträge stellen oder annehmen.",
                        "Rechts siehst du verfügbare Jobs."
                ),
                Arrays.asList(
                        "Schließlich ist hier der Hafen mit einem schwarzen Brett für noch mehr Jobangebote.",
                        "Das war's mit der Stadttour. Viel Spaß auf dem Server!"
                ),
                false,
                null,
                null
        ));
    }

    public static void startCutscene(ServerPlayer player, int index) {
        if (index >= goals.size()) {
            playerCutsceneIndex.remove(player.getUUID());
            return;
        }

        Goal goal = goals.get(index);
        for (String msg : goal.pre) {
            player.sendSystemMessage(Component.literal(msg).withStyle(style -> style.withColor(0xFFAA00))); // Gold
        }
        if (goal.preAction != null) {
            goal.preAction.accept(player);
        }
        playerCutsceneIndex.put(player.getUUID(), index);
    }

    public static void tick(ServerPlayer player) {
        if (!player.getTags().contains("rpn.tutorial_finished")) {
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "tag " + player.getScoreboardName() + " add rpn.tutorial_finished");
            startCutscene(player, 0);
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), "team join Player " + player.getScoreboardName());
        }

        if (playerCutsceneIndex.containsKey(player.getUUID())) {
            int index = playerCutsceneIndex.get(player.getUUID());
            if (index < 0 || index >= goals.size()) {
                playerCutsceneIndex.remove(player.getUUID());
                return;
            }

            Goal goal = goals.get(index);
            Vec3 playerPos = player.position();
            double distance = Math.sqrt(
                    Math.pow(playerPos.x - goal.location.x, 2) +
                    Math.pow(playerPos.y - goal.location.y, 2) +
                    Math.pow(playerPos.z - goal.location.z, 2)
            );

            if (distance <= 3) {
                for (String msg : goal.after) {
                    player.sendSystemMessage(Component.literal(msg).withStyle(style -> style.withColor(0x55FF55))); // Green
                }

                if (goal.afterAction != null) {
                    goal.afterAction.accept(player);
                }

                if (goal.next) {
                    startCutscene(player, index + 1);
                } else {
                    playerCutsceneIndex.remove(player.getUUID());
                }
            } else {
                int minParticles = 20;
                int maxParticles = 200;
                double maxDistance = 200;
                int particleCount = (int) Math.min(
                        Math.max(
                                Math.floor(distance / maxDistance * maxParticles),
                                minParticles
                        ),
                        maxParticles
                );

                double dx = goal.location.x - playerPos.x;
                double dy = goal.location.y + 1.1 - playerPos.y;
                double dz = goal.location.z - playerPos.z;

                for (int i = 0; i < particleCount; i++) {
                    double t = (double) i / (particleCount - 1);
                    double x = playerPos.x + dx * t;
                    double y = playerPos.y + dy * t + 0.3;
                    double z = playerPos.z + dz * t;

                    // Using command for particles as it's easier than finding the packet or server method sometimes, 
                    // but we can use server level spawnParticles.
                    // However, the script used "force" mode which sends to specific player.
                    // player.serverLevel().sendParticles(player, ...)
                    
                    // Let's stick to command for simplicity and exact replication of behavior
                    String cmd = String.format("particle minecraft:dust 0 1 0 0.7 %.2f %.2f %.2f 0 0 0 0.01 1 force %s", x, y, z, player.getScoreboardName());
                    player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4), cmd);
                }
            }
        }
    }
    
    public static int getGoalsCount() {
        return goals.size();
    }
}
