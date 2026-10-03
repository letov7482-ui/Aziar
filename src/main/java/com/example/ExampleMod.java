package com.example;

import com.example.module.ModuleManager;
import com.example.gui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    public static ModuleManager moduleManager;
    public static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        moduleManager = new ModuleManager();
        moduleManager.init();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.examplemod.gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "ExampleMod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            if (client.player != null && client.world != null) {
                moduleManager.onTick();
            }
        });
    }
}
