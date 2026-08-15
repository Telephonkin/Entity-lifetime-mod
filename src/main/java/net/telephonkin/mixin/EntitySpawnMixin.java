package net.telephonkin.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.telephonkin.EntityLifeTimeMod;
import net.telephonkin.data.EntityLifeTimeTable;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static net.telephonkin.data.EntityLifeTimeTableProperPut.putProperly;

@Mixin(ServerWorld.class)
public abstract class EntitySpawnMixin {

	@Shadow @Final private MinecraftServer server;

	//@Unique
	//private static final Logger LOGGER = LoggerFactory.getLogger("entity-lifetime-mod");
	@Unique
	private static HashMap<String, Integer> LOADED_MOD_ENTITY_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig();

	@Inject(method = "spawnEntity", at = @At("HEAD"))
	public void onEntitySpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		// Server-side logic, which represents entity natural spawn
		if (!entity.getWorld().isClient()) {

			//Iterable<ServerWorld> worlds = server.getWorlds();
			ServerWorld overworld = server.getOverworld();

			EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(overworld);
			long birthdate;

			String entityTypeString = entity.getType().toString().substring(7).replace(".",":");
			try {
				System.out.println(((Number) LOADED_MOD_ENTITY_CONFIG.get(entityTypeString)).intValue());
			} catch (Exception e) {
				System.out.println("failed to get and entity type: " + entityTypeString);
			}


			if (((Number) LOADED_MOD_ENTITY_CONFIG.get(entityTypeString)).intValue() != -1) {
				// Write data about entity UUID and birth time to the table
				birthdate = server.getTicks();

				entity_birth_table.setMap(putProperly(
						LOADED_MOD_ENTITY_CONFIG,
						entity_birth_table.getMap(),
						entity,
						entity.getUuid(),
						birthdate));
				entity_birth_table.markDirty();
			}

		}

	}

	/*@Inject(method = "onPlayerConnect", at = @At("HEAD"))
	private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		// Your custom logic here
		// Example: System.out.println(player.getName().getString() + " joined!");
	}*/
}