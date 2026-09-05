package net.telephonkin;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.DefaultEntityConfig;
import net.telephonkin.data.EntityLifeTimeTable;
import net.telephonkin.data.SavedEntityLifeTimeCounter;
import net.telephonkin.data.ToDespawnEntityCacheHashSet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class EntityLifeTimeMod implements ModInitializer {
	public static final String MOD_ID = "entity-lifetime-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.

	private ServerWorld world;
	private EntityLifeTimeTable entity_birth_table;
	private HashMap<String, Integer> loadedEntityConfig;
	private SavedEntityLifeTimeCounter savedEntityLifeTimeCounter;

	public HashMap<String, Integer> getLoadedEntityConfig() {
		return loadedEntityConfig;
	}

	public void setLoadedEntityConfig(HashMap<String, Integer> loadedEntityConfig) {
		this.loadedEntityConfig = loadedEntityConfig;
	}

	public AtomicLong getTimer() {
		return timer;
	}

	public void setTimer(long input_value) {
		timer.set(input_value);
	}

	public void setCurrentEntityUUID(UUID input_value) {
		currentEntityUUID.set(input_value);
	}

	public AtomicReference<Map.Entry<UUID, HashMap<String, Long>>> getFirstEntity() {
		return first_entity;
	}

	public AtomicReference<Map.Entry<UUID, HashMap<String, Long>>> getSecondEntity() {
		return first_entity;
	}

	public void setSecondEntity(AtomicReference<Map.Entry<UUID, HashMap<String, Long>>> input_value) {
		secondEntity.set(input_value.get());
	}


	public AtomicLong timer = new AtomicLong();
	public AtomicReference<UUID> currentEntityUUID = new AtomicReference<>(null);

	public AtomicReference<Map.Entry<UUID, HashMap<String, Long>>> first_entity = new AtomicReference<>();
	public AtomicReference<Map.Entry<UUID, HashMap<String, Long>>> secondEntity = new AtomicReference<>(null);

	public static EntityLifeTimeMod INSTANCE;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		INSTANCE = this;

		// Load the config data
		try {
			this.setLoadedEntityConfig(DefaultEntityConfig.config.loadEntityConfig());
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

		EntityDespawner entityDespawner = new EntityDespawner();
		AtomicReference<UUID> currentEntityUUID = new AtomicReference<>(null);
		AtomicLong currentEntitySpawnTime = new AtomicLong();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			world = server.getOverworld();
			SavedEntityLifeTimeCounter savedEntityLifeTimeCounter = SavedEntityLifeTimeCounter.get(world);
			entity_birth_table = EntityLifeTimeTable.get(world);
			LinkedHashMap<UUID, HashMap<String, Long>> entity_birth_table_as_table = entity_birth_table.entityLifeTimeTable;
			HashSet<UUID> toDespawnEntities = new HashSet<>();
			currentEntitySpawnTime.set(0L);
			//System.out.println("timer is " + timer.get());
			// If entity_birth_table_as_table is empty - do nothing
			//System.out.println(timer);
			if (entity_birth_table_as_table.isEmpty()) {
				savedEntityLifeTimeCounter.setValue(0L);
			} else {

				first_entity.set(entity_birth_table_as_table.entrySet().iterator().next());
				// Get the first entity UUID after the beginning of lifetime counter

				if (timer.get() == 0L){

					if (currentEntityUUID.get() != null) {
						// The first entity is defined (is in entity_birth_table_as_table), so let's despawn it

						// The first entity exist, so call EntityDespawner to despawn ALL entities with the same time of despawn
						currentEntityUUID.set(first_entity.get().getKey());
						// Make a list of all entities to despawn
						entity_birth_table.getMap().forEach((entity_UUID_in_table, entity_params_in_table) -> {
							// If this entity despawns in one time as first - add to the list
							if (entity_UUID_in_table != first_entity.get().getKey()) {
								String entity_type_in_table = entity_params_in_table.entrySet().iterator().next().getKey();
								if (
                                        Objects.equals(
												entity_params_in_table.entrySet().iterator().next().getValue(),
												entity_birth_table.getMap().get(currentEntityUUID.get()).entrySet().iterator().next().getValue())
										&&
										Objects.equals(
												entity_type_in_table,
												first_entity.get().getValue().entrySet().iterator().next().getKey()
										)
								) {
									toDespawnEntities.add(entity_UUID_in_table);
								}
							}
						});
						toDespawnEntities.add(currentEntityUUID.get());

						// Using toDespawnEntities list - despawn each entity

						currentEntitySpawnTime.set(entity_birth_table.getMap().get(currentEntityUUID.get()).entrySet().iterator().next().getValue());
						
						toDespawnEntities.forEach(toDespawnEntity -> {
							try {
								entityDespawner.loadedChunksDespawner(
										server,
										toDespawnEntity
								);
							} catch (RuntimeException e) {
								throw e;
							}
							// Delete entity from table
							entity_birth_table.removeItem(toDespawnEntity);
							entity_birth_table.markDirty();
						});
						toDespawnEntities.clear();

						// After despawning entity, calculate the timer
					}

					try {
						secondEntity.set((Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table.getMap().entrySet().toArray()[1]);
					} catch (RuntimeException ignored) {}
					// Get the second entity in the name
					if (secondEntity.get() != null) {
						secondEntity.set((Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table.getMap().entrySet().toArray()[1]);

						// Set the timer
						// The first case: second entity spawns after first entity despawns (in other words: time now <= birthdate of the second entity) - the timer equals the lifetime of the second entity
						// The second case: the opposite one (the timer equals the time second entity lives - (time of the first entity despawn - time of the second entity birth ))
						if (server.getTicks() <= secondEntity.get().getValue().entrySet().iterator().next().getValue()) {
							// The first case
							String entity_type_2 = secondEntity.get().getValue().keySet().iterator().next();
							Number entity_lifetime_raw_2 = loadedEntityConfig.get(entity_type_2);
							timer.set(entity_lifetime_raw_2.longValue());

						} else {
							// The second case
							String entity_type_1 = first_entity.get().getValue().keySet().iterator().next();
							String entity_type_2 = secondEntity.get().getValue().keySet().iterator().next();
							Number entity_lifetime_raw_1 = loadedEntityConfig.get(entity_type_1);
							Number entity_lifetime_raw_2 = loadedEntityConfig.get(entity_type_2);

							timer.set(
									(secondEntity
											.get()
											.getValue()
											.entrySet()
											.iterator()
											.next()
											.getValue()
									-
									first_entity
											.get()
											.getValue()
											.entrySet()
											.iterator()
											.next()
											.getValue())
									+
									Math.abs(entity_lifetime_raw_1.longValue() - entity_lifetime_raw_2.longValue()));
							savedEntityLifeTimeCounter.setValue(timer.get());
							savedEntityLifeTimeCounter.markDirty();
						}

						// Set the second entity characteristics to the first_entity variable

						first_entity.set(secondEntity.get());

					} else {
						// This is the case when there is only one entity and timer is 0
						if (timer.get() == 0L) {
							String entity_type = first_entity.get().getValue().keySet().iterator().next();
							Number entity_lifetime_raw = loadedEntityConfig.get(entity_type);

							timer.set(entity_lifetime_raw.longValue());
							currentEntityUUID.set(first_entity.get().getKey());

							try {
								secondEntity.set((Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table_as_table.entrySet().toArray()[1]);
							} catch (RuntimeException ignored) {}
						}
					}
				} else {
					timer.getAndDecrement();
				}
			}
		});

		// Triggered when the server is shutting down
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			world = server.getOverworld();

			EntityLifeTimeTable.get(world).setMap(entity_birth_table.getMap());
			EntityLifeTimeTable.get(world).markDirty();

			savedEntityLifeTimeCounter = SavedEntityLifeTimeCounter.get(world);
			savedEntityLifeTimeCounter.setValue(timer.get());
			savedEntityLifeTimeCounter.markDirty();
		// Example: saving custom data, closing database connections
		});

		// Delete entity when chunk, where it placed, loads
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (ToDespawnEntityCacheHashSet.get(world).getSet().contains(entity.getUuid())) {
				// Delete entity from the world
				entity.discard();
				// Remove entity from cache
				ToDespawnEntityCacheHashSet.get(world).removeUUID(entity.getUuid());
				ToDespawnEntityCacheHashSet.get(world).markDirty();

			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			MinecraftServer server = null;
			if (entity.getWorld() instanceof ServerWorld serverWorld) {
				server = serverWorld.getServer();
			}
			try {
				if (entity.getUuid() == first_entity.get().getKey() || entity.getUuid() == secondEntity.get().getKey()) {
					// Remove entity from the table if a player kill it
					entity_birth_table.removeItem(entity.getUuid());
					// Recalculate timer
					TimerRecalculator timerRecalculator = new TimerRecalculator();
					timerRecalculator.process(
							server,
							entity_birth_table
					);
				} else {
					entity_birth_table.removeItem(entity.getUuid());
					entity_birth_table.markDirty();
				}
			} catch (Exception e) {
				// This is the case when there is only one entity and timer is 0
				if (timer.get() == 0L) {
					String entity_type = first_entity.get().getValue().keySet().iterator().next();
					Number entity_lifetime_raw = loadedEntityConfig.get(entity_type);

					timer.set(entity_lifetime_raw.longValue());
					currentEntityUUID.set(first_entity.get().getKey());

					try {
						secondEntity.set((Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table.getMap().entrySet().toArray()[1]);
					} catch (RuntimeException ignored) {}
				}
			}
		});
	}
}