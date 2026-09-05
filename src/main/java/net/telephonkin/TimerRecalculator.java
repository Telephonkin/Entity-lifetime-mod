package net.telephonkin;

import net.minecraft.server.MinecraftServer;
import net.telephonkin.data.EntityLifeTimeTable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class TimerRecalculator {
    public void process(
        MinecraftServer server,
        EntityLifeTimeTable entity_birth_table

    ){
        // The first case: second entity spawns after first entity despawns (in other words: time now <= birthdate of the second entity) - the timer equals the lifetime of the second entity
        // The second case: the opposite one (the timer equals the time second entity lives - (time of the first entity despawn - time of the second entity birth ))

        if (EntityLifeTimeMod.INSTANCE.getSecondEntity() != null) {
            if (server.getTicks() <= EntityLifeTimeMod.INSTANCE.getSecondEntity().get().getValue().entrySet().iterator().next().getValue()) {
                // The first case
                String entity_type_2 = EntityLifeTimeMod.INSTANCE.getSecondEntity().get().getValue().keySet().iterator().next();
                Number entity_lifetime_raw_2 = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig().get(entity_type_2);
                EntityLifeTimeMod.INSTANCE.setTimer(entity_lifetime_raw_2.longValue());
                //timer.set(entity_lifetime_raw_2.longValue());

            } else {
                // The second case
                String entity_type_1 = EntityLifeTimeMod.INSTANCE.getFirstEntity().get().getValue().keySet().iterator().next();
                String entity_type_2 = EntityLifeTimeMod.INSTANCE.getSecondEntity().get().getValue().keySet().iterator().next();
                Number entity_lifetime_raw_1 = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig().get(entity_type_1);
                Number entity_lifetime_raw_2 = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig().get(entity_type_2);

                EntityLifeTimeMod.INSTANCE.setTimer(
                        (EntityLifeTimeMod.INSTANCE.getSecondEntity()
                                .get()
                                .getValue()
                                .entrySet()
                                .iterator()
                                .next()
                                .getValue()
                                -
                                EntityLifeTimeMod.INSTANCE.getFirstEntity().get()
                                        .getValue()
                                        .entrySet()
                                        .iterator()
                                        .next()
                                        .getValue())
                                +
                                Math.abs(entity_lifetime_raw_1.longValue() - entity_lifetime_raw_2.longValue()));
            }
        } else {
            // This is the case when there is only one entity and timer is 0
            if (EntityLifeTimeMod.INSTANCE.getTimer().get() == 0L) {
                String entity_type = EntityLifeTimeMod.INSTANCE.getFirstEntity().get().getValue().keySet().iterator().next();
                Number entity_lifetime_raw = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig().get(entity_type);

                EntityLifeTimeMod.INSTANCE.setTimer(entity_lifetime_raw.longValue());
                EntityLifeTimeMod.INSTANCE.setCurrentEntityUUID(EntityLifeTimeMod.INSTANCE.getFirstEntity().get().getKey());

                try {
                    EntityLifeTimeMod.INSTANCE.setSecondEntity((AtomicReference<Map.Entry<UUID, HashMap<String, Long>>>) entity_birth_table.getMap().entrySet().toArray()[1]);
                } catch (RuntimeException ignored) {}
            }
        }
    }
}
