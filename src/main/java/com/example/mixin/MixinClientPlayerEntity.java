package com.example.mixin;

import com.example.ExampleMod;
import com.example.module.combat.Aura;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    // Позволяет камере свободно двигаться при активной Aura
    // Не блокируем input — только визуально показываем что смотрим на цель
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (ExampleMod.moduleManager == null) return;

        Aura aura = (Aura) ExampleMod.moduleManager.getByName("Aura");
        if (aura != null && aura.isEnabled() && aura.getCurrentTarget() != null) {
            // Камера свободна — Aura работает в фоне через пакеты
            // Server видит что мы смотрим на цель, клиент свободен
        }
    }
}
