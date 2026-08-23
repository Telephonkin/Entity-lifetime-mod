package net.telephonkin;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.DefaultEntityConfig;
import net.telephonkin.data.EntityLifeTimeTable;
import net.telephonkin.data.SavedEntityLifeTimeCounter;
import net.telephonkin.data.ToDespawnEntityCacheHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class EntityLifeTimeMod implements ModInitializer {
	public static final String MOD_ID = "entity-lifetime-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ServerWorld world;
	private EntityLifeTimeTable entity_birth_table;
	private HashMap<String, Integer> LoadedEntityConfig;
	//private final LifetimeCounter lifetimeCounter = new LifetimeCounter();
	private SavedEntityLifeTimeCounter savedEntityLifeTimeCounter;
	private HashSet<UUID> toDespawnEntityCacheHashSet;


	public ServerWorld getWorld() {
		return this.world;
	}

	public void setWorld(ServerWorld world) {
		this.world = world;
	}

	//public long getSavedEntityLifeTimeCounter() {
	//	return savedEntityLifeTimeCounter.getValue();
	//}

	//public void setSavedEntityLifeTimeCounter(long timer) {
	//	this.savedEntityLifeTimeCounter.setValue(timer);
	//}

	public EntityLifeTimeTable getentity_birth_table() {
		return entity_birth_table;
	}

	public void set_entity_birth_table(EntityLifeTimeTable entity_birth_table) {
		this.entity_birth_table = entity_birth_table;
	}

	public HashMap<String, Integer> getLoadedEntityConfig() {
		return LoadedEntityConfig;
	}

	public void setLoadedEntityConfig(HashMap<String, Integer> loadedEntityConfig) {
		LoadedEntityConfig = loadedEntityConfig;
	}

	public ToDespawnEntityCacheHashSet getToDespawnEntityCacheHashSet(ServerWorld world){return ToDespawnEntityCacheHashSet.get(world);}

	//public void setToDespawnEntityCacheHashSet(ToDespawnEntityCacheHashSet toDespawnEntityCacheHashSet) {
	//	this.toDespawnEntityCacheHashSet = toDespawnEntityCacheHashSet;
	//}

	public static EntityLifeTimeMod INSTANCE;
	//static public entityLifeTimeMod getInstance() { return INSTANCE; }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		INSTANCE = this;

		// Load the config data
		try {
			this.setLoadedEntityConfig(DefaultEntityConfig.config.loadEntityConfig());
			//HashMap<String, Integer> loaded_config = DefaultConfig.config.loadConfig();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
		//SavedEntityLifeTimeCounter savedEntityLifeTimeCounter = SavedEntityLifeTimeCounter.get(world);

		// Triggered when a player finishes joining the server
		/*ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			this.setWorld(server.getOverworld());

			//this.setTimer(0L);
			this.set_entity_birth_table(EntityLifeTimeTable.get(world));
			System.out.println("start counter");
            try {
                lifetimeCounter.processStart(
                        server,
                        LoadedEntityConfig
                );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });*/



		AtomicLong timer = new AtomicLong();
		//long time_now = 0;
		EntityDespawner entityDespawner = new EntityDespawner();
		//AtomicBoolean is_only_one_entity_to_set_timer = new AtomicBoolean(true);

		AtomicReference<Map.Entry<UUID, HashMap<String, Long>>> first_entity = new AtomicReference<>();
		AtomicReference<Map.Entry<UUID, HashMap<String, Long>>> second_entity = new AtomicReference<>(null);

		AtomicReference<UUID> currentEntityUUID = new AtomicReference<>(null);

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ServerWorld world = server.getOverworld();
			SavedEntityLifeTimeCounter savedEntityLifeTimeCounter = SavedEntityLifeTimeCounter.get(world);
			System.out.println("Time on the moment is "+timer);
			EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
			//System.out.println("" + entity_birth_table.getMap());
			LinkedHashMap<UUID, HashMap<String, Long>> entity_birth_table_as_table = entity_birth_table.entityLifeTimeTable;
			System.out.println("Size of table is: "+entity_birth_table_as_table.size());
			HashSet<UUID> toDespawnEntities = new HashSet<>();

			long time_now = server.getTicks();
			long currentEntitySpawnTime = 0L;

			// If entity_birth_table_as_table is empty - do nothing
			if (entity_birth_table_as_table.isEmpty()) {
				System.out.println("No spawned entity");
				savedEntityLifeTimeCounter.setValue(0L);
				//continue;
			} else {

				first_entity.set(entity_birth_table_as_table.entrySet().iterator().next());
				// Get the first entity UUID after the beginning of lifetime counter


				if (timer.get() == 0L){
					//if (is_only_one_entity_to_set_timer.get() == false) {
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
						System.out.println("Mod is going to despawn entities" + toDespawnEntities);
						// TODO finish for each loops

						// Using toDespawnEntities list - despawn each entity

						currentEntitySpawnTime = entity_birth_table.getMap().get(currentEntityUUID.get()).entrySet().iterator().next().getValue();
						
						toDespawnEntities.forEach(toDespawnEntity -> {
							try {
								entityDespawner.loadedChunksDespawner(
										world,
										//world.getEntity(currentEntityUUID.get())
										Objects.requireNonNull(world.getEntity(toDespawnEntity))
								);
							} catch (RuntimeException ignored) {}
							System.out.println("Entity " + toDespawnEntity + " was despawned");
							// Delete entity from table
							entity_birth_table.removeItem(toDespawnEntity);
							entity_birth_table.markDirty();
							//entity_birth_table_as_table.remove(toDespawnEntity);
						});
						toDespawnEntities.clear();

						// After despawning entity, calculate the timer
					}

					try {
						second_entity.set((Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table.getMap().entrySet().toArray()[1]);
					} catch (RuntimeException ignored) {}
					//is_only_one_entity_to_set_timer = false;
					// Get the second entity in the name
					if (second_entity.get() != null) {
						second_entity.set((Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table.getMap().entrySet().toArray()[1]);

						// Set the timer
						// The first case: second entity spawns after first entity despawns (in other words: time now <= birthdate of the second entity) - the timer equals the lifetime of the second entity
						// The second case: the opposite one (the timer equals the time second entity lives - (time of the first entity despawn - time of the second entity birth ))
						System.out.println(entity_birth_table.getMap());
						if (time_now <= currentEntitySpawnTime) {
							// The first case
							System.out.println("Set timer 1 case");
							String entity_type_1 = first_entity.get().getValue().keySet().iterator().next();
							String entity_type_2 = second_entity.get().getValue().keySet().iterator().next();
							Number entity_lifetime_raw_1 = LoadedEntityConfig.get(entity_type_1);
							Number entity_lifetime_raw_2 = LoadedEntityConfig.get(entity_type_2);
							timer.set((long) entity_lifetime_raw_2.longValue());
							savedEntityLifeTimeCounter.setValue(timer.get()
									/*LoadedEntityConfig.get(
											second_entity.get()
													.getValue()
													.keySet()
													.toString()
									)*/
							);
							savedEntityLifeTimeCounter.markDirty();
						} else {
							// The second case
							System.out.println("Set timer 2 case");
							String entity_type_1 = first_entity.get().getValue().keySet().iterator().next();
							String entity_type_2 = second_entity.get().getValue().keySet().iterator().next();
							Number entity_lifetime_raw_1 = LoadedEntityConfig.get(entity_type_1);
							Number entity_lifetime_raw_2 = LoadedEntityConfig.get(entity_type_2);
							System.out.println("Second entity lives: "+ entity_lifetime_raw_2.longValue() + " Time now: " + time_now + " First entity live " + entity_lifetime_raw_1.longValue() + " Second entity spawn time: " + second_entity.get()
									.getValue()
									.entrySet()
									.iterator()
									.next()
									.getValue());
							timer.set(
									(second_entity
											.get()
											.getValue()
											.entrySet()
											.iterator()
											.next()
											.getValue() -
									first_entity
											.get()
											.getValue()
											.entrySet()
											.iterator()
											.next()
											.getValue()) + Math.abs(entity_lifetime_raw_1.longValue() - entity_lifetime_raw_2.longValue()));

							savedEntityLifeTimeCounter.setValue(timer.get()
									/*LoadedEntityConfig.get(
											second_entity
													.getValue()
													.keySet()
													.toString()
									) - (
											time_now + LoadedEntityConfig.get(
													first_entity
															.getValue()
															.keySet()
															.toString()
											) - second_entity
													.getValue()
													.entrySet()
													.iterator()
													.next()
													.getValue()
									)*/
							);
							savedEntityLifeTimeCounter.markDirty();
						}

						// Set the second entity characteristics to the first_entity variable

						first_entity.set(second_entity.get());

					} else {
						// This is the case when there is only one entity and timer is 0
						if (timer.get() == 0L) {
							String entity_type = first_entity.get().getValue().keySet().iterator().next();
							Number entity_lifetime_raw = LoadedEntityConfig.get(entity_type);

							//System.out.println(LoadedEntityConfig.get((String) first_entity.getValue().keySet().iterator().next())+" "
							//		+LoadedEntityConfig.get((String) first_entity.getValue().entrySet().iterator().next().getKey().toString().getClass().getName()));

							//timer.set((long) LoadedEntityConfig.get((String) first_entity.getValue().entrySet().iterator().next().getKey().toString())
							System.out.println("Set timer 3 case");
							timer.set((long) entity_lifetime_raw.longValue());
							currentEntityUUID.set(first_entity.get().getKey());

							try {
								second_entity.set((Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table_as_table.entrySet().toArray()[1]);
							} catch (RuntimeException ignored) {}
						}
					}
				} else {
					//timer.set(savedEntityLifeTimeCounter.getValue());
					System.out.println("timer before decrement "+timer);
					timer.getAndDecrement();
					System.out.println("timer after decrement "+timer);
					savedEntityLifeTimeCounter.setValue(timer.get());
					savedEntityLifeTimeCounter.markDirty();
				}
			}
		});

		// Triggered when the server is shutting down
		//ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

			//savedEntityLifeTimeCounter
			// Your logic when the server is about to shut down
			// Example: saving custom data, closing database connections
		//});

		// Delete entity when chunk, where it placed, loads
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (ToDespawnEntityCacheHashSet.get(world).getSet().contains(entity.getUuid())) {
				// Delete entity from the world
				System.out.println("entity got dispawned from loaded chunk " + entity.getType().toString() + " with UUID " + entity.getUuidAsString());
				entity.discard();
				// Remove entity from cache
				ToDespawnEntityCacheHashSet.get(world).removeUUID(entity.getUuid());
				ToDespawnEntityCacheHashSet.get(world).markDirty();

			}
		});

        LOGGER.info("Hello Fabric world!");
	}
}