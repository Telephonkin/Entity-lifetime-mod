package net.telephonkin;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.DefaultEntityConfig;
import net.telephonkin.data.EntityLifeTimeTable;
import net.telephonkin.data.SavedEntityLifeTimeCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

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
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			this.setWorld(server.getOverworld());
			//this.setTimer(0L);
			this.set_entity_birth_table(EntityLifeTimeTable.get(world));
			System.out.println("start counter");
			lifetimeCounter.processStart(
					server,
					LoadedEntityConfig
			);
		});

		// Triggered when the server is shutting down
		//ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

			//savedEntityLifeTimeCounter
			// Your logic when the server is about to shut down
			// Example: saving custom data, closing database connections
		//});

		// Delete entity when chunk, where it placed, loads

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!world.isClient() && entity instanceof Entity entity1) {

			}
		});

        LOGGER.info("Hello Fabric world!");
	}
}