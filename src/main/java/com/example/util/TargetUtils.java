package com.example.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import java.util.ArrayList;
import java.util.List;

public class TargetUtils {

    public static List<LivingEntity> getTargetsInRange(double range) {
        List<LivingEntity> targets = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return targets;

        for (Entity e : mc.world.getEntities()) {
            if (e instanceof LivingEntity living && e != mc.player && living.isAlive()) {
                if (mc.player.distanceTo(living) <= range) {
                    targets.add(living);
                }
            }
        }
        return targets;
    }
}
