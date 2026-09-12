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

import java.util.*;

import static net.telephonkin.data.EntityLifeTimeTableProperPut.putProperly;

@Mixin(ServerWorld.class)
public abstract class EntitySpawnMixin {

	@Shadow @Final private MinecraftServer server;

	@Unique
	private static HashMap<String, Integer> LOADED_MOD_ENTITY_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedEntityConfig();

	@Inject(method = "spawnEntity", at = @At("HEAD"))
	public void onEntitySpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		// Server-side logic, which represents entity natural spawn
		if (!entity.getWorld().isClient()) {
			System.out.println(entity.getType());

			if (entity instanceof TntEntity tntEntity || entity instanceof ItemEntity itemEntity) {
				// Do nothing here; go to TntEntityMixin
				//if (LOADED_MOD_ENTITY_CONFIG.get("minecraft:tnt") != -1) {
				//	Number tntLifetime = LOADED_MOD_ENTITY_CONFIG.get("minecraft:tnt");
					//System.out.println("TNT was spawned, time is: " + LOADED_MOD_ENTITY_CONFIG.get("minecraft:tnt"));
				//	//tntEntity.setFuse(tntLifetime.intValue());
				//	tntEntity.setFuse(200);
				//}

			//} else if (entity instanceof ItemEntity itemEntity) {
			//	if (((Number)LOADED_MOD_ENTITY_CONFIG.get("minecraft:item")).intValue() != -1) {
			//			Number itemLifetime = LOADED_MOD_ENTITY_CONFIG.get("minecraft:item");
			//			System.out.println("Item was spawned, time is: " + itemLifetime.intValue());
			//			itemEntity.age = itemLifetime.intValue();
			//		}
			} else {
					ServerWorld overworld = server.getOverworld();

					EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(overworld);
					long birthdate;

					String entityTypeString = entity.getType().toString().substring(7).replace(".",":");
					try {
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
					} catch (Exception e) {
						System.out.println(entityTypeString);
					}
				}
			}
		}
	}
