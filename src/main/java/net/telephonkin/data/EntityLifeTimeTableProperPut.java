package net.telephonkin.data;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public class EntityLifeTimeTableProperPut {

    private static final Logger LOGGER = LoggerFactory.getLogger("entity-lifetime-mod");

    public static LinkedHashMap<UUID, Long> putProperly(
            HashMap<String, Integer> LOADED_MOD_CONFIG,
            ServerWorld world,
            LinkedHashMap<UUID, Long> input_map,
            Entity entity,
            UUID to_put_uuid,
            Long to_put_birth_time)  {


        LinkedHashMap<UUID, Long> reversed_entity_list = new LinkedHashMap<UUID, Long>(input_map.reversed());
        LinkedHashMap<UUID, Long> output_reversed = new LinkedHashMap<UUID, Long>();

        final boolean[] is_new_entity_added = {false};
        reversed_entity_list.forEach((key, value) -> {
            String entity_type_from_map = Objects.requireNonNull(world.getEntity(key)).getType().toString();
            LOGGER.info("ENTITY TYPE IS :{}", entity_type_from_map);
            //System.out.println("ENTITY TYPE IS :" + entity_type_from_map);
            String new_entity_type = entity.getType().toString();
            int how_much_does_new_entity_live = LOADED_MOD_CONFIG.get(new_entity_type);
            int how_much_does_entity_from_map_live = LOADED_MOD_CONFIG.get(entity_type_from_map);

            if (is_new_entity_added[0]) {
                output_reversed.put(key, value);
            } else {
                if (value + (long) how_much_does_entity_from_map_live > to_put_birth_time + (long) how_much_does_new_entity_live) { // Means that new spawned entity will be despawned before than previous entity, so we need to place it before and check again
                    output_reversed.put(key, value);
                } else { // Means that new spawned entity will be despawned after than previous entity, so put new entity to the end
                    output_reversed.put(key, value);
                    output_reversed.put(to_put_uuid, to_put_birth_time);
                    is_new_entity_added[0] = true;

                }
            }
        });
        return new LinkedHashMap<UUID, Long>(output_reversed.reversed());
    }
}
