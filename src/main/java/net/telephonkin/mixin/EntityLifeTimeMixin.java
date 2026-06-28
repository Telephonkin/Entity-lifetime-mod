package net.telephonkin.mixin;

import it.unimi.dsi.fastutil.Hash;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.telephonkin.EntityLifeTimeMod;
import net.telephonkin.data.EntityLifeTimeTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.BiConsumer;

import static net.telephonkin.data.EntityLifeTimeTableProperPut.putProperly;

@Mixin(ServerWorld.class)
public abstract class EntityLifeTimeMixin {

	@Shadow @Final private MinecraftServer server;
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("entity-lifetime-mod");
	@Unique
	private static HashMap<String, Integer> LOADED_MOD_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedConfig();

	@Inject(method = "spawnEntity", at = @At("HEAD"))
	public void onEntitySpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		// Server-side logic, which represents entity natural spawn
		if (!entity.getWorld().isClient()) {

			ServerWorld world = server.getOverworld();
			EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
			long birthdate;

			String entityTypeString = entity.getType().toString().substring(7).replace(".",":");
			//String entityTypeString = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
			LOGGER.info("ENTITY TYPE IS :{}", entityTypeString);
			//LOGGER.info("LIFETIME:{}", LOADED_MOD_CONFIG.get(entityTypeString));
			//LOGGER.info("LIFETIME:{}", (String) LOADED_MOD_CONFIG.get(entityTypeString).getClass().getName());
			//System.out.println("ENTITY TYPE IS :" + entityTypeString);
			//int not_spawn = -1;
			//LOADED_MOD_CONFIG.keySet().getClass().getTypeName();
			if (((Number) LOADED_MOD_CONFIG.get(entityTypeString)).intValue() != -1) {
				// write data about entity UUID and birthtime to the table
				birthdate = server.getTicks();

				entity_birth_table.setMap(putProperly(
						LOADED_MOD_CONFIG,
						world,
						entity_birth_table.getMap(),
						entity,
						entity.getUuid(),
						birthdate));
				//entity_birth_table.entityLifeTimeTable.put(entity.getUuid(), birthdate);
				System.out.println("Table:" + entity_birth_table.getMap());
				//System.out.println("A Creeper has spawned with UUID:" + entity.getUuidAsString() + "at " + birthdate);
				entity_birth_table.markDirty();
			}

		}

	}
}