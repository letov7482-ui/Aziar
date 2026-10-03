package com.example.module.combat;

import com.example.module.Module;
import com.example.util.RotationUtils;
import com.example.util.TargetUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Aura extends Module {

    public enum TargetMode {
        PLAYERS_AND_MOBS,
        PLAYERS_ONLY,
        MOBS_ONLY
    }

    // --- Settings ---
    public TargetMode targetMode = TargetMode.PLAYERS_AND_MOBS;
    public float attackRange = 4.5f;
    public float rotationSpeed = 15.0f;   // плавность поворота (градусов за тик)
    public int attackDelay = 10;          // тики между атаками (20 = 1 сек)
    public boolean smoothRotation = true; // плавный поворот камеры
    public boolean ignoreElytra = true;   // не работать в элитрах

    private int attackCooldown = 0;
    private LivingEntity currentTarget = null;

    // Для анти-бан: имитация реального игрока
    private float lastYaw = 0f;
    private float lastPitch = 0f;

    public Aura() {
        super("Aura");
    }

    @Override
    protected void onEnable() {
        attackCooldown = 0;
        currentTarget = null;
    }

    @Override
    protected void onDisable() {
        currentTarget = null;
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) return;

        // Не работать в элитрах
        if (ignoreElytra && player.isFallFlying()) return;

        // Поиск цели
        currentTarget = findTarget();
        if (currentTarget == null) return;

        // Поворот к цели
        rotateToTarget(player, currentTarget);

        // Атака
        if (attackCooldown <= 0) {
            attack(player, currentTarget);
            attackCooldown = attackDelay;
        }

        if (attackCooldown > 0) attackCooldown--;
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = attackRange;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (e == mc.player) continue;
            if (!living.isAlive()) continue;

            // Фильтр по режиму
            switch (targetMode) {
                case PLAYERS_ONLY -> { if (!(living instanceof net.minecraft.entity.player.PlayerEntity)) continue; }
                case MOBS_ONLY -> { if (living instanceof net.minecraft.entity.player.PlayerEntity) continue; }
                case PLAYERS_AND_MOBS -> {}
            }

            double dist = mc.player.distanceTo(living);
            if (dist < bestDist) {
                // Проверка что цель видна
                if (mc.player.canSee(living)) {
                    best = living;
                    bestDist = dist;
                }
            }
        }
        return best;
    }

    private void rotateToTarget(ClientPlayerEntity player, LivingEntity target) {
        // Вычисляем углы до цели
        Vec3d targetPos = target.getEyePos();
        Vec3d playerPos = player.getEyePos();

        double dx = targetPos.x - playerPos.x;
        double dy = targetPos.y - playerPos.y;
        double dz = targetPos.z - playerPos.z;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDist)));

        targetYaw = MathHelper.wrapDegrees(targetYaw);
        targetPitch = MathHelper.clamp(targetPitch, -90.0f, 90.0f);

        if (smoothRotation) {
            // Плавный поворот — не мгновенный
            float yawDiff = MathHelper.wrapDegrees(targetYaw - player.getYaw());
            float pitchDiff = MathHelper.wrapDegrees(targetPitch - player.getPitch());

            float yawStep = MathHelper.clamp(yawDiff, -rotationSpeed, rotationSpeed);
            float pitchStep = MathHelper.clamp(pitchDiff, -rotationSpeed, rotationSpeed);

            // Добавляем человеческую неточность для анти-бан
            float jitter = (mc.player.getRandom().nextFloat() - 0.5f) * 0.5f;

            player.setYaw(player.getYaw() + yawStep + jitter);
            player.setPitch(player.getPitch() + pitchStep + jitter * 0.3f);
        } else {
            player.setYaw(targetYaw);
            player.setPitch(targetPitch);
        }

        lastYaw = player.getYaw();
        lastPitch = player.getPitch();
    }

    private void attack(ClientPlayerEntity player, LivingEntity target) {
        // Проверка что держим оружие
        ItemStack heldItem = player.getMainHandStack();
        if (!(heldItem.getItem() instanceof SwordItem) && !(heldItem.getItem() instanceof AxeItem)) {
            return;
        }

        // Анти-бан: атакуем через пакет, не напрямую
        // Имитируем нормальную последовательность клиента
        mc.player.networkHandler.sendPacket(
            PlayerInteractEntityC2SPacket.attack(target, player.isSneaking())
        );

        // Свинг руки (визуально и по пакету)
        player.swingHand(Hand.MAIN_HAND);
        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        // Анти-бан: критический удар только если падаем
        // Не спамим криты как дешёвые читы
        if (player.fallDistance > 0.0f && !player.isOnGround() && !player.isClimbing() &&
            !player.isTouchingWater() && !player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)) {
            // Уже идёт нативный крит
        }
    }

    // Геттеры для GUI
    public TargetMode getTargetMode() { return targetMode; }
    public void setTargetMode(TargetMode mode) { this.targetMode = mode; }
    public float getAttackRange() { return attackRange; }
    public void setAttackRange(float range) { this.attackRange = MathHelper.clamp(range, 1.0f, 6.0f); }
    public float getRotationSpeed() { return rotationSpeed; }
    public void setRotationSpeed(float speed) { this.rotationSpeed = MathHelper.clamp(speed, 1.0f, 45.0f); }
    public int getAttackDelay() { return attackDelay; }
    public void setAttackDelay(int delay) { this.attackDelay = Math.max(1, delay); }
    public boolean isSmoothRotation() { return smoothRotation; }
    public void setSmoothRotation(boolean smooth) { this.smoothRotation = smooth; }

    public LivingEntity getCurrentTarget() { return currentTarget; }
                                }
