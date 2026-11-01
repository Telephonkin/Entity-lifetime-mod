package net.telephonkin.mixin;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.entity.ItemEntity;
import net.telephonkin.EntityLifeTimeMod;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemEntity.class)
public class ItemEntityAgeMixin {

    @Shadow private int itemAge;

    @Unique
    private static HashMap<String, HashMap<String,Object>> LOADED_MOD_ENTITY_CONFIG = new HashMap<String, HashMap<String, Object>>(EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig());

    @Inject(method = "tick", at = @At("TAIL"))
    private void changeDespawnTimer(CallbackInfo ci) {
        // Here the age time of an item entity changes
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        // Ensure we are operating on the server logic side
        if (!itemEntity.getWorld().isClient()) {
            // So when an item achieves age as it set in entity config - discard it
            if (((Number) LOADED_MOD_ENTITY_CONFIG.get("minecraft:item").get("lifetime")).intValue() != -1) {
                if (itemEntity.getItemAge() >= (((Number) LOADED_MOD_ENTITY_CONFIG.get("minecraft:item").get("lifetime")).intValue())) {
                    itemEntity.discard();
                } else {
                    // Otherwise, if age is set to -1 - it should live forever
                    this.itemAge = -32768;
                }
            }
        }
    }
}