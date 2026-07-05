package net.telephonkin.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
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
            Iterable<ServerWorld> worlds,
            MinecraftServer server,
            LinkedHashMap<UUID, Long> input_map,
            Entity entity,
            UUID to_put_uuid,
            Long to_put_birth_time)  {


        LinkedHashMap<UUID, Long> reversed_entity_list = new LinkedHashMap<UUID, Long>(input_map.reversed());
        LinkedHashMap<UUID, Long> output_reversed = new LinkedHashMap<UUID, Long>();

        final boolean[] is_new_entity_added = {false};
        if (input_map.isEmpty()) {
            input_map.put(to_put_uuid, to_put_birth_time);
            LOGGER.info("The Table is Empty");
            return input_map;
        } else {
            reversed_entity_list.forEach((key, value) -> {
                //String entity_type_from_map = "";
                String entity_type_from_map = null;
                //try {
                //    //server.get
                //    //entity_type_from_map = Objects.requireNonNull(world.getEntity(key)).getType().toString();
                //    //entity_type_from_map = server.getCommandSource().getEntityOrThrow().getType().toString();
                //} catch (CommandSyntaxException e) {
                //    throw new RuntimeException(e);
                //}
                //Entity entity2 = server.getCommandSource().getEntityOrThrow();
                for (ServerWorld world: worlds) {
                    try {
                        entity_type_from_map = Objects.requireNonNull(world.getEntity(key)).getType().toString().substring(7).replace(".", ":");
                        break;
                    } catch (Exception e) {
                        LOGGER.info("WHATS WRONG " );
                    }
                }
                    //    //entity_type_from_map = Objects.requireNonNull(world.getEntity(key)).getType().toString().substring(7).replace(".", ":");
                //    entity_type_from_map = server.getCommandSource().getEntityOrThrow().getType().toString().substring(7).replace(".", ":");
                //} catch (Exception e) {
                //    LOGGER.info("WHATS WRONG " );
                //}
                    LOGGER.info("SPAWNED ENTITY TYPE IS : {}", entity_type_from_map);
                //System.out.println("ENTITY TYPE IS :" + entity_type_from_map);
                String new_entity_type = entity.getType().toString().substring(7).replace(".",":");
                long how_much_does_entity_from_map_live = 0L;
                long how_much_does_new_entity_live = 0L;

                //String entityTypeString = entity.getType().toString().substring(7).replace(".",":");

                try {
                    how_much_does_new_entity_live = (long) ((Number) LOADED_MOD_CONFIG.get(new_entity_type)).intValue();
                            //Long.valueOf(LOADED_MOD_CONFIG.get(new_entity_type));
                    how_much_does_entity_from_map_live = (long) ((Number) LOADED_MOD_CONFIG.get(entity_type_from_map)).intValue();
                            //Long.valueOf(LOADED_MOD_CONFIG.get(entity_type_from_map));
                } catch (Exception e) {
                    LOGGER.info("REASON " + LOADED_MOD_CONFIG.get(entity_type_from_map) + " and ENTITY type " + entity_type_from_map);
                }
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
            System.out.println("output table" + output_reversed.reversed().toString());
            return new LinkedHashMap<UUID, Long>(output_reversed.reversed());
        }
    }
}
