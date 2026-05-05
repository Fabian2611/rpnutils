package io.fabianbuthere.rpnutils.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class RPNUtilsConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BANNED_ITEMS;

    static {
        BUILDER.push("General");

        BANNED_ITEMS = BUILDER
                .comment("List of banned resource IDs for items")
                .defineList("banned_items", Collections.emptyList(), o -> o instanceof String);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
