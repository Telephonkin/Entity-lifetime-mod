package net.telephonkin.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import net.telephonkin.EntityLifeTimeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin {

    @Shadow public abstract void setFuse(int fuse);

    @Unique
    private static HashMap<String, Integer> LOADED_MOD_ENTITY_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig();
    // Here the fuse time of a tnt entity changes
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V", at = @At("TAIL"))
    private void changeFuseTime(World world, double x, double y, double z, LivingEntity igniter, CallbackInfo ci) {
        if (((Number) LOADED_MOD_ENTITY_CONFIG.get("minecraft:tnt")).intValue() != -1) {
            this.setFuse(((Number) LOADED_MOD_ENTITY_CONFIG.get("minecraft:tnt")).intValue());
        }
    }
}