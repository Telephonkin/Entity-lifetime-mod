package net.telephonkin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.EntityLifeTimeTable;
import net.telephonkin.data.SavedEntityLifeTimeCounter;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class LifetimeCounter {
	// TODO make a EntityDespawner class which has 3 ways to despawn an entity: when player load chunk, when mod loads the chunk entity-to-despawn exist and the despawn, despawn through editing world data. Edit these modes via entity_lifetime_common_config.json5

	public void processStart(
			MinecraftServer server,
			HashMap<String,Integer> ENTITY_CONFIG
			//long timer
	) {
		//String currentEntityType = "";
		System.out.println("starting counter");
		new Thread(()-> {
			while (true){
				ServerWorld world = server.getOverworld();
				EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
				AtomicLong timer = new AtomicLong();
				SavedEntityLifeTimeCounter savedEntityLifeTimeCounter = SavedEntityLifeTimeCounter.get(world);
				long time_now = server.getTicks();
				//long time_now;
				UUID currentEntityUUID = null;
				LinkedHashMap<UUID, HashMap<String, Long>> entity_birth_table_as_table = entity_birth_table.entityLifeTimeTable;

				EntityDespawner entityDespawner = new EntityDespawner();

				Map.Entry<UUID, HashMap<String, Long>> first_entity = null;
				Map.Entry<UUID, HashMap<String, Long>> second_entity = null;


				System.out.println("counter started");
				// If entity_birth_table_as_table is empty - do nothing
				if (entity_birth_table_as_table.isEmpty()) {
					System.out.println("The time is 0!");
					savedEntityLifeTimeCounter.setValue(0L);
					continue;
				} else {
					System.out.println("The table is not empty");
					// Set the second entity
					//Map.Entry<UUID, HashMap<String, Long>> first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
					//currentEntityUUID = first_entity.getKey();

					// Reduce the timer to 0
					while (savedEntityLifeTimeCounter.getValue()!=0L) {
						timer.set(savedEntityLifeTimeCounter.getValue());
						timer.getAndDecrement();
						savedEntityLifeTimeCounter.setValue(timer.get());
						savedEntityLifeTimeCounter.markDirty();
					}
					// Despawn the first entity , but if entity is null, so try to get an entity from the table, and then try again

					// Get the first entity UUID after the beginning of lifetime counter
					if (first_entity != null) {
						currentEntityUUID = first_entity.getKey();
					}

					// At the beginning, it may exist the situation when there is no first entity (entity table just created some moments ago)
					if (currentEntityUUID == null) {
						System.out.println("No entity");
						// Do not call EntityDespawner, because there is no first entity
						// Try to set the first entity, otherwise, do the loop from the beginning
						try {
							first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
						} catch (Exception e) {
							continue;
						}
						continue;
					} else {
						// The first entity is defined (is in entity_birth_table_as_table), so let's despawn it
						System.out.println("Mod is going to despawn entity");
						// The first entity exist, so call EntityDespawner
						entityDespawner.loadedChunksDespawner(
								world,
								Objects.requireNonNull(world.getEntity(currentEntityUUID))
						);
						System.out.println("Entity" + currentEntityUUID + "was despawned");
						// Delete entity from table
						entity_birth_table.entityLifeTimeTable.remove(currentEntityUUID);
						// After despawning entity, calculate the timer

						// Get the second entity in the name
						second_entity = (Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table.entityLifeTimeTable.entrySet().toArray()[1];
						// Set the timer
						// The first case: second entity spawns after first entity despawns (in other words: time now <= birthdate of the second entity) - the timer equals the lifetime of the second entity
						// The second case: the opposite one (the timer equals the time second entity lives - (time of the first entity despawn - time of the second entity birth ))
						if (time_now <= entity_birth_table.getMap().get(currentEntityUUID).entrySet().iterator().next().getValue()) {
							// The first case
							savedEntityLifeTimeCounter.setValue(ENTITY_CONFIG.get(second_entity.getValue().keySet().toString()));
							savedEntityLifeTimeCounter.markDirty();
						} else {
							// The second case
							savedEntityLifeTimeCounter.setValue(ENTITY_CONFIG.get(second_entity.getValue().keySet().toString()) - (time_now + ENTITY_CONFIG.get(first_entity.getValue().keySet().toString()) - second_entity.getValue().entrySet().iterator().next().getValue()));
							savedEntityLifeTimeCounter.markDirty();
						}

						// Set the second entity characteristics to the first_entity variable
						first_entity = second_entity;
						continue;
					}

					// Set the next entity that will be despawned
					//Map.Entry<UUID, HashMap<String, Long>> first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
					//currentEntityUUID = first_entity.getKey();

					// Set the timer
					//if (time_now + timer <= entity_birth_table.getMap().get(currentEntityUUID).entrySet().iterator().next().getValue()) {
					//	timer = ENTITY_CONFIG.get(currentEntityType);
					//} else {
					//	timer =
					//}
					}
				}
			}
		);

		//try {
		//	first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
		//} catch (Exception e) {
		//	System.out.println("EXCEPTION" + entity_birth_table.entityLifeTimeTable.toString());
		//}
        //assert first_entity != null;
        //Entity entity = world.getEntity(first_entity.getKey());
	}
}
