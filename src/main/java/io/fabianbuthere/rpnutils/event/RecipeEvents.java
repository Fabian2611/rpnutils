package io.fabianbuthere.rpnutils.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpnutils", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecipeEvents {
    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        RecipeManager manager = event.getRecipeManager();

        manager.byKey(ResourceLocation.fromNamespaceAndPath("quark", "tweaks/crafting/utility/bent/bread"))
                .ifPresent(recipe -> manager.getRecipes().remove(recipe));
    }
}
