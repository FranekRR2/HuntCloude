package dev.huntcloude;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class HuntCloudeClient implements ClientModInitializer {
    private static KeyMapping menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.huntcloude.menu", GLFW.GLFW_KEY_M, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) {
                State.noclip = false;
                return;
            }
            while (menuKey.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new MenuScreen());
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("huntcloude", "captions"),
                (graphics, tickCounter) -> Hud.render(graphics));
    }
}
