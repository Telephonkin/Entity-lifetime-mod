package net.telephonkin;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.DefaultConfig;
import net.telephonkin.data.EntityLifeTimeTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EntityLifeTimeMod implements ModInitializer {
	public static final String MOD_ID = "entity-lifetime-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ServerWorld world;
	private long timer;
	private EntityLifeTimeTable entity_birth_table;

	public ServerWorld getWorld() {
		return this.world;
	}

	public void setWorld(ServerWorld world) {
		this.world = world;
	}

	public long getTimer() {
		return timer;
	}

	public void setTimer(long timer) {
		this.timer = timer;
	}

	public EntityLifeTimeTable getentity_birth_table() {
		return entity_birth_table;
	}

	public void setentity_birth_table(EntityLifeTimeTable entity_birth_table) {
		this.entity_birth_table = entity_birth_table;
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
			DefaultConfig.config.loadConfig();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Triggered when a player finishes joining the server
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			this.setWorld(server.getOverworld());
			this.setTimer(0L);
			this.setentity_birth_table(EntityLifeTimeTable.get(world));
		});



        LOGGER.info("Hello Fabric world!");
	}
}