package net.telephonkin.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.telephonkin.TimerRecalculator;
import net.telephonkin.data.EntityLifeTimeTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(Entity.class)
public class EntityNameMixin {

    @Inject(method = "setCustomName", at = @At("HEAD"))
    private void onSetCustomName(Text name, CallbackInfo ci) {
        // When entity got named - delete from the table
        Entity entity = (Entity) (Object) this;
        TimerRecalculator timerRecalculator = new TimerRecalculator();
        if (!entity.getWorld().isClient()) {
            UUID entityUUID = entity.getUuid();

            MinecraftServer server = entity.getServer();
            EntityLifeTimeTable entityLifeTimeTable = EntityLifeTimeTable.get(server.getOverworld());

            Map.Entry<UUID, HashMap<String, Long>> first_entity = (Map.Entry<UUID, HashMap<String, Long>>) entityLifeTimeTable.getMap().entrySet().toArray()[0];
            Map.Entry<UUID, HashMap<String, Long>> second_entity = (Map.Entry<UUID, HashMap<String, Long>>) entityLifeTimeTable.getMap().entrySet().toArray()[1];

            // Check that this entity is not first or second entity in the table, otherwise - recalculate timer
            if (
                    first_entity.getKey() == entityUUID
                    || second_entity.getKey() == entityUUID
            ) {
                // Recalculate timer

                timerRecalculator.process(
                        server,
                        entityLifeTimeTable
                );
            } else {
                entityLifeTimeTable.removeItem(entityUUID);
                entityLifeTimeTable.markDirty();
            }


        }
    }
}