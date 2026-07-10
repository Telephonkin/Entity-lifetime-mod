package net.telephonkin.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

public class EntityLifeTimeTableProperPut {

    private static final Logger LOGGER = LoggerFactory.getLogger("entity-lifetime-mod");

    public static LinkedHashMap<UUID, HashMap<String, Long>> putProperly(
            HashMap<String, Integer> LOADED_MOD_CONFIG,
            Iterable<ServerWorld> worlds,
            MinecraftServer server,
            LinkedHashMap<UUID, HashMap<String, Long>> input_map,
            Entity entity,
            UUID to_put_uuid,
            Long to_put_birth_time)  {


        LinkedHashMap<UUID, HashMap<String, Long>> reversed_entity_list = new LinkedHashMap<UUID, HashMap<String, Long>>(input_map.reversed());
        LinkedHashMap<UUID, HashMap<String, Long>> output_reversed = new LinkedHashMap<UUID, HashMap<String, Long>>();

        final boolean[] is_new_entity_added = {false};
        if (input_map.isEmpty()) {
            String entity_type = entity.getType().toString().substring(7).replace(".",":");
            HashMap<String, Long> second_part = new HashMap<>();
            second_part.put(entity_type,to_put_birth_time);
            output_reversed.put(
                    to_put_uuid, second_part);
            LOGGER.info("The Table is Empty" + output_reversed);
            return output_reversed;
        } else {
            reversed_entity_list.forEach((key, value) -> {
                //System.out.println("key" + key);
                //System.out.println("value" + value);
                String entity_type_from_map = input_map.get(key).entrySet().iterator().next().getKey();
                //System.out.println(entity_type_from_map);
                /*for (ServerWorld world: worlds) {
                    //System.out.println(Objects.requireNonNull(world.getEntity(key)).getType().toString());
                    try {
                        //System.out.println(Objects.requireNonNull(world.getEntity(key)).getType().toString());
                        //if (Objects.requireNonNull(world.getEntity(key)).getType() != null) {
                            entity_type_from_map = Objects
                                    .requireNonNull(
                                            world.getEntity(key))
                                    .getType()
                                    .toString()
                                    .substring(7)
                                    .replace(".", ":");

                            break;
                        //} else {
                            //System.out.println("In world" + world.toString() + "there is no entity with UUID" + to_put_uuid.toString());
                            //continue;
                        //}
                    } catch (NullPointerException e){
                        continue;
                    }
                }*/
                String new_entity_type = entity.getType().toString().substring(7).replace(".",":");
                long how_much_does_entity_from_map_live = 0L;
                long how_much_does_new_entity_live = 0L;

                //try {
                    how_much_does_new_entity_live = (long) ((Number) LOADED_MOD_CONFIG.get(new_entity_type)).intValue();
                    //System.out.println();
                    //System.out.println(entity_type_from_map);
                    how_much_does_entity_from_map_live = (long) ((Number) LOADED_MOD_CONFIG.get(entity_type_from_map)).intValue();
                //} catch (Exception e) {
                //    LOGGER.info("REASON " + LOADED_MOD_CONFIG.get(entity_type_from_map) + " and ENTITY type " + entity_type_from_map);
                //}
                if (is_new_entity_added[0]) {
                    output_reversed.put(key, value);
                } else {
                    if (value.get(entity_type_from_map) + (long) how_much_does_entity_from_map_live > to_put_birth_time + (long) how_much_does_new_entity_live) { // Means that new spawned entity will be despawned before than previous entity, so we need to place it before and check again
                        output_reversed.put(key, value);
                    } else { // Means that new spawned entity will be despawned after than previous entity, so put new entity to the end
                        output_reversed.put(key, value);
                        HashMap<String, Long> output_second_part = new HashMap<>();
                        output_second_part.put(entity_type_from_map,to_put_birth_time);
                        output_reversed.put(to_put_uuid, output_second_part);
                        is_new_entity_added[0] = true;

                    }
                }
            });
            System.out.println("output table" + output_reversed.reversed().toString());
            return new LinkedHashMap<UUID, HashMap<String, Long>>(output_reversed.reversed());
        }
    }
}
