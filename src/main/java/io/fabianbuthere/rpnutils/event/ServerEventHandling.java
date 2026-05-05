package io.fabianbuthere.rpnutils.event;

import io.fabianbuthere.rpnutils.RPNUtilMod;
import io.fabianbuthere.rpnutils.config.RPNUtilsConfig;
import io.fabianbuthere.rpnutils.util.CutsceneHandler;
import io.fabianbuthere.rpnutils.util.PersonalausweisHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = RPNUtilMod.MOD_ID)
public class ServerEventHandling {
    private static final ResourceLocation[] toRemove = new ResourceLocation[]{
            resourceLocation("quark", "tweaks/crafting/utility/bent/bread"),
            resourceLocation("minecraft", "bread"),
            resourceLocation("minecraft", "oak_planks"),
            resourceLocation("minecraft", "spruce_planks"),
            resourceLocation("minecraft", "birch_planks"),
            resourceLocation("minecraft", "jungle_planks"),
            resourceLocation("minecraft", "acacia_planks"),
            resourceLocation("minecraft", "dark_oak_planks"),
            resourceLocation("minecraft", "mangrove_planks"),
            resourceLocation("minecraft", "cherry_planks"),
            resourceLocation("minecraft", "crimson_planks"),
            resourceLocation("minecraft", "warped_planks"),
            resourceLocation("quark", "world/crafting/woodsets/ancient/planks"),
            resourceLocation("quark", "world/crafting/woodsets/azalea/planks"),
            resourceLocation("create", "cutting/stripped_oak_log"),
            resourceLocation("create", "cutting/stripped_oak_wood"),
            resourceLocation("create", "cutting/stripped_spruce_log"),
            resourceLocation("create", "cutting/stripped_spruce_wood"),
            resourceLocation("create", "cutting/stripped_birch_log"),
            resourceLocation("create", "cutting/stripped_birch_wood"),
            resourceLocation("create", "cutting/stripped_jungle_log"),
            resourceLocation("create", "cutting/stripped_jungle_wood"),
            resourceLocation("create", "cutting/stripped_acacia_log"),
            resourceLocation("create", "cutting/stripped_acacia_wood"),
            resourceLocation("create", "cutting/stripped_dark_oak_log"),
            resourceLocation("create", "cutting/stripped_dark_oak_wood"),
            resourceLocation("create", "cutting/stripped_mangrove_log"),
            resourceLocation("create", "cutting/stripped_mangrove_wood"),
            resourceLocation("create", "cutting/stripped_cherry_log"),
            resourceLocation("create", "cutting/stripped_cherry_wood"),
            resourceLocation("create", "cutting/stripped_crimson_stem"),
            resourceLocation("create", "cutting/stripped_crimson_hyphae"),
            resourceLocation("create", "cutting/stripped_warped_stem"),
            resourceLocation("create", "cutting/stripped_warped_hyphae"),
            resourceLocation("farmersdelight", "wheat_dough_from_water")
    };

    private static final Set<ResourceLocation> itemsToRemove = Set.of(
            resourceLocation("dye_depot", "maroon_shulker_box"),
            resourceLocation("dye_depot", "rose_shulker_box"),
            resourceLocation("dye_depot", "coral_shulker_box"),
            resourceLocation("dye_depot", "ginger_shulker_box"),
            resourceLocation("dye_depot", "tan_shulker_box"),
            resourceLocation("dye_depot", "beige_shulker_box"),
            resourceLocation("dye_depot", "olive_shulker_box"),
            resourceLocation("dye_depot", "amber_shulker_box"),
            resourceLocation("dye_depot", "forest_shulker_box"),
            resourceLocation("dye_depot", "verdant_shulker_box"),
            resourceLocation("dye_depot", "teal_shulker_box"),
            resourceLocation("dye_depot", "aqua_shulker_box"),
            resourceLocation("dye_depot", "mint_shulker_box"),
            resourceLocation("dye_depot", "navy_shulker_box"),
            resourceLocation("dye_depot", "slate_shulker_box"),
            resourceLocation("dye_depot", "indigo_shulker_box")
    );

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        RecipeManager recipeManager = event.getServer().getRecipeManager();

        updateBannedItems();

        RPNUtilMod.LOGGER.info("Removing conflicting recipes...");

        try {
            Field byNameField = RecipeManager.class.getDeclaredField("byName");
            byNameField.setAccessible(true);

            Field recipesField = RecipeManager.class.getDeclaredField("recipes");
            recipesField.setAccessible(true);

            Map<ResourceLocation, Recipe<?>> byNameMap = (Map<ResourceLocation, Recipe<?>>) byNameField.get(recipeManager);
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipesMap =
                    (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);

            Map<ResourceLocation, Recipe<?>> newByName = new HashMap<>(byNameMap);
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>();

            recipesMap.forEach((type, map) -> newRecipes.put(type, new HashMap<>(map)));

            for (ResourceLocation recipeId : toRemove) {
                if (newByName.remove(recipeId) != null) {
                    RPNUtilMod.LOGGER.info("Removed recipe: {}", recipeId);
                    newRecipes.values().forEach(map -> map.remove(recipeId));
                } else {
                    RPNUtilMod.LOGGER.warn("Recipe not found (could not remove): {}", recipeId);
                }
            }

            newByName.entrySet().removeIf(entry -> {
                ResourceLocation resultItem = ForgeRegistries.ITEMS.getKey(entry.getValue()
                        .getResultItem(event.getServer().registryAccess()).getItem());

                if (itemsToRemove.contains(resultItem)) {
                    RPNUtilMod.LOGGER.info("Removed recipe: {} (produces {})", entry.getKey(), resultItem);
                    newRecipes.values().forEach(map -> map.remove(entry.getKey()));
                    return true;
                }
                return false;
            });

            byNameField.set(recipeManager, newByName);
            recipesField.set(recipeManager, newRecipes);

            RPNUtilMod.LOGGER.info("Recipe removal complete.");

        } catch (Exception e) {
            RPNUtilMod.LOGGER.error("Failed to remove recipes", e);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            RecipeManager recipeManager = serverPlayer.getServer().getRecipeManager();
            serverPlayer.connection.send(new ClientboundUpdateRecipesPacket(
                    recipeManager.getRecipes()
            ));
        }
    }

    private static final Set<Item> BANNED_ITEMS_SET = new HashSet<>();

    public static void updateBannedItems() {
        BANNED_ITEMS_SET.clear();
        RPNUtilMod.LOGGER.debug("Updating banned items.");
        for (String id : RPNUtilsConfig.BANNED_ITEMS.get()) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(id));
            if (item == null) {
                RPNUtilMod.LOGGER.warn("Failed to find to-be-banned item: {}", id);
            } else {
                BANNED_ITEMS_SET.add(item);
                RPNUtilMod.LOGGER.debug("Banned item: {}", id);
            }
        }
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        RPNUtilMod.LOGGER.info("Loading config: Updating banned items.");
        updateBannedItems();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        RPNUtilMod.LOGGER.info("Reloading config: Updating banned items.");
        updateBannedItems();
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (isItemBanned(event.getItem().getItem())) {
            event.setCanceled(true);
            event.getItem().kill();
            event.getEntity().sendSystemMessage(Component.literal("Dieses Item ist verboten!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
    }

    @SubscribeEvent
    public static void onPlayerContainerOpen(PlayerContainerEvent.Open event) {
        event.getContainer().slots.forEach((slot) -> {
            if (isItemBanned(slot.getItem())) {
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
                event.getEntity().sendSystemMessage(Component.literal("Ein verbotenes Item wurde gelöscht!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerItemUse(PlayerInteractEvent.RightClickItem event) {
        if (isItemBanned(event.getItemStack())) {
            event.setCanceled(true);
            event.getItemStack().setCount(0);
            event.getEntity().sendSystemMessage(Component.literal("Dieses Item ist verboten!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(PlayerInteractEvent.RightClickBlock event) {
        if (isItemBanned(event.getItemStack())) {
            event.setCanceled(true);
            event.getItemStack().setCount(0);
            event.getEntity().sendSystemMessage(Component.literal("Dieses Item ist verboten!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
    }

    @SubscribeEvent
    public static void onItemCraft(PlayerEvent.ItemCraftedEvent event) {
        if (isItemBanned(event.getCrafting())) {
            event.setCanceled(true);
            event.getEntity().sendSystemMessage(Component.literal("Dieses Item ist verboten!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
    }

    private static boolean isItemBanned(Item item) {
        return BANNED_ITEMS_SET.contains(item);
    }

    private static boolean isItemBanned(ItemStack stack) {
        return isItemBanned(stack.getItem());
    }

    @SuppressWarnings("removal")
    private static ResourceLocation resourceLocation(final String namespace, final String path) {
        return ResourceLocation.of(namespace + ":" + path, ':');
    }
}
