package dev.agentsharik.fallenrelics;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * The JEI side of the Chat & Recipe Linker module: JEI hands us its runtime here,
 * and the client logic below uses it to read the ingredient under the mouse,
 * drive the search box and open the recipe gui. Without JEI nothing is loaded.
 */
@JeiPlugin
public final class JeiLinkerPlugin implements IModPlugin {
    @Nullable
    private static volatile IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(FallenRelicsMod.MOD_ID, "jei_chat_linker");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    @Nullable
    static IJeiRuntime runtime() {
        return runtime;
    }
}
