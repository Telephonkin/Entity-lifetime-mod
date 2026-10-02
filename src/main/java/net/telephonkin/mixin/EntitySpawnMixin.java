package net.telephonkin.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.telephonkin.EntityLifeTimeMod;
import net.telephonkin.data.EntityLifeTimeTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.telephonkin.data.EntityLifeTimeTableProperPut.putProperly;

@Mixin(ServerWorld.class)
public abstract class EntitySpawnMixin {

	@Shadow @Final private MinecraftServer server;

	@Unique
	private static HashMap<String, Integer> LOADED_MOD_ENTITY_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig();

	@Unique
	private static HashMap<String, Object> LOADED_MOD_COMMON_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedCommonConfig();

	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("Entity Lifetime");

	@Inject(method = "spawnEntity", at = @At("HEAD"))
	public void onEntitySpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		// Server-side logic, which represents entity natural spawn
		if (!entity.getWorld().isClient()) {

			// Common setting entity spawn logging
			// Convert ArrayList to HashSet
			List<String> logsConfigAsList = (ArrayList<String>) LOADED_MOD_COMMON_CONFIG.get("logs");
			HashSet<String> logsConfig = new HashSet<String>(logsConfigAsList);
			if (logsConfig.contains("spawn")) {
				LOGGER.info("Spawned at time " + server.getOverworld().getTime() + " Entity with a type " + entity.getType().toString() + " and UUID " + entity.getUuid().toString());
			}

			if (entity instanceof TntEntity tntEntity || entity instanceof ItemEntity itemEntity) {
				// Do nothing here; go to TntEntityMixin
			} else {
					ServerWorld overworld = server.getOverworld();

					EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(overworld);
					long birthdate;

					String entityTypeString = entity.getType().toString().substring(7).replace(".",":");
					try {
						if (((Number) LOADED_MOD_ENTITY_CONFIG.get(entityTypeString)).intValue() != -1) {
							// Write data about entity UUID and birth time to the table
							birthdate = overworld.getTime();

							entity_birth_table.setMap(putProperly(
									LOADED_MOD_ENTITY_CONFIG,
									entity_birth_table.getMap(),
									entity,
									entity.getUuid(),
									birthdate));
							entity_birth_table.markDirty();
						}
					} catch (Exception e) {}
				}
			}
		}
	}
