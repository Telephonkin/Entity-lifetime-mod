package net.telephonkin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.EntityLifeTimeTable;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class LifetimeCounter {
	//@Final private MinecraftServer server;

	// TODO make a EntityDespawner class which has 3 ways to despawn an entity: when player load chunk, when mod loads the chunk entity-to-despawn exist and the despawn, despawn through editing world data. Edit these modes via entity_lifetime_common_config.json5

	public void processStart(
			MinecraftServer server
	) {
		ServerWorld world = server.getOverworld();
		EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
		long timer = 0L;
		//long time_now = server.getTicks();
		//long time_now;
		UUID currentEntityUUID = null;
		LinkedHashMap<UUID, HashMap<String, Long>> entity_birth_table_as_table = entity_birth_table.entityLifeTimeTable;

		while (timer!=0L) {
			if (entity_birth_table_as_table.isEmpty()) {
				timer = 0L;
			} else {
				// Check that time is 0L : if it is, despawn the entity , but if entity is null, so try to get an entity from the table, and then try again
				if (timer != 0L) {
					// Kill current the entity
					if (currentEntityUUID == null) {
						// Do not call EntityDespawner, because there is no entity
					} else {
						// Entity exist, so call EntityDespawner

						// Delete entity from table


					}
					// Set the next entity that will be despawned
					Map.Entry<UUID, HashMap<String, Long>> first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
					currentEntityUUID = first_entity.getKey();
					// Set the timer
				}
			}
		}
		//try {
		//	first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
		//} catch (Exception e) {
		//	System.out.println("EXCEPTION" + entity_birth_table.entityLifeTimeTable.toString());
		//}
        //assert first_entity != null;
        //Entity entity = world.getEntity(first_entity.getKey());
	}
}
