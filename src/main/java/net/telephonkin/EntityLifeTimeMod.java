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
	private final LifetimeCounter lifetimeCounter = new LifetimeCounter();
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
        /*
		// Triggered when a player finishes joining the server
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
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
        });
		*/


		AtomicLong timer = new AtomicLong();
		//long time_now = 0;
		EntityDespawner entityDespawner = new EntityDespawner();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ServerWorld world = server.getOverworld();
			SavedEntityLifeTimeCounter savedEntityLifeTimeCounter = SavedEntityLifeTimeCounter.get(world);

			EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
			LinkedHashMap<UUID, HashMap<String, Long>> entity_birth_table_as_table = entity_birth_table.entityLifeTimeTable;

			UUID currentEntityUUID = null;
			Map.Entry<UUID, HashMap<String, Long>> first_entity = null;
			Map.Entry<UUID, HashMap<String, Long>> second_entity = null;
			long time_now = server.getTicks();

			// If entity_birth_table_as_table is empty - do nothing
			if (entity_birth_table_as_table.isEmpty()) {
				System.out.println("No spawned entity");
				savedEntityLifeTimeCounter.setValue(0L);
				//continue;
			} else {

				first_entity = entity_birth_table_as_table.entrySet().iterator().next();
				// Get the first entity UUID after the beginning of lifetime counter
				currentEntityUUID = first_entity.getKey();

				timer.set(savedEntityLifeTimeCounter.getValue());
				timer.getAndDecrement();
				savedEntityLifeTimeCounter.setValue(timer.get());
				savedEntityLifeTimeCounter.markDirty();
				if (timer.get()  == 0L){
					// The first entity is defined (is in entity_birth_table_as_table), so let's despawn it
					System.out.println("Mod is going to despawn entity");
					// The first entiity exist, so call EntityDespawner
					entityDespawner.loadedChunksDespawner(
							world,
							Objects.requireNonNull(world.getEntity(currentEntityUUID))
					);
					System.out.println("Entity" + currentEntityUUID + "was despawned");
					// Delete entity from table
					entity_birth_table_as_table.remove(currentEntityUUID);
					// After despawning entity, calculate the timer

					// Get the second entity in the name
					try {
						second_entity = (Map.Entry<UUID, HashMap<String, Long>>) entity_birth_table_as_table.entrySet().toArray()[1];

						// Set the timer
						// The first case: second entity spawns after first entity despawns (in other words: time now <= birthdate of the second entity) - the timer equals the lifetime of the second entity
						// The second case: the opposite one (the timer equals the time second entity lives - (time of the first entity despawn - time of the second entity birth ))
						if (time_now <= entity_birth_table.getMap().get(currentEntityUUID).entrySet().iterator().next().getValue()) {
							// The first case
							savedEntityLifeTimeCounter.setValue(
									LoadedEntityConfig.get(
											second_entity
													.getValue()
													.keySet()
													.toString()
									)
							);
							savedEntityLifeTimeCounter.markDirty();
						} else {
							// The second case
							savedEntityLifeTimeCounter.setValue(
									LoadedEntityConfig.get(
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
									)
							);
							savedEntityLifeTimeCounter.markDirty();
						}

						// Set the second entity characteristics to the first_entity variable

						first_entity = second_entity;

					} catch (RuntimeException ignored) {}
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