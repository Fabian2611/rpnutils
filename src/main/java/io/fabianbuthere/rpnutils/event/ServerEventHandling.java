package io.fabianbuthere.rpnutils.event;

import io.fabianbuthere.rpnutils.RPNUtilMod;
import io.fabianbuthere.rpnutils.util.CutsceneHandler;
import io.fabianbuthere.rpnutils.util.PersonalausweisHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPNUtilMod.MOD_ID)
public class ServerEventHandling {

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();
        
        if (PersonalausweisHandler.handleInput(player, message)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer serverPlayer) {
            CutsceneHandler.tick(serverPlayer);
        }
    }
}
