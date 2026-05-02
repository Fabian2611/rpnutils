package io.fabianbuthere.rpnutils.event;

import io.fabianbuthere.rpnutils.RPNUtilMod;
import io.fabianbuthere.rpnutils.data.PaymentSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPNUtilMod.MOD_ID)
public class PaymentTicker {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.side.isServer() && event.phase == TickEvent.Phase.END && event.level.dimension() == Level.OVERWORLD &&
                event.level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 1200 == 0
        ) {
            RPNUtilMod.LOGGER.info("RPNUtils tick");
            PaymentSavedData.get(serverLevel).tick(serverLevel);
        }
    }
}
