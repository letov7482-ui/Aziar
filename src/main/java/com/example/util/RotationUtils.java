package com.example.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {

    public static float[] getRotations(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontal)));
        return new float[] { MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f) };
    }

    public static float getAngleDifference(float a, float b) {
        return Math.abs(MathHelper.wrapDegrees(a - b));
    }
}
