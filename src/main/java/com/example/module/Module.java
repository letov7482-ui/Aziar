package com.example.module;

import net.minecraft.client.MinecraftClient;

public abstract class Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private final String name;
    private boolean enabled;

    public Module(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
}
