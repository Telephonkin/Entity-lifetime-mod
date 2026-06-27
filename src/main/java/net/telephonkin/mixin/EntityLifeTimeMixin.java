package net.telephonkin.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.telephonkin.EntityLifeTimeMod;
import net.telephonkin.data.EntityLifeTimeTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(ServerWorld.class)
public abstract class EntityLifeTimeMixin {

	@Shadow @Final private MinecraftServer server;

	@Inject(method = "spawnEntity", at = @At("HEAD"))
	public void onEntitySpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		// Server-side logic, which represents entity natural spawn
		if (!entity.getWorld().isClient()) {

			ServerWorld world = server.getOverworld();
			EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
			long birthdate;
			HashMap<String, Integer> CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedConfig();

			String entityTypeString = Registries.ENTITY_TYPE.getId(entity.getType()).toString();

			if (CONFIG.get(entityTypeString) != -1) {
				// write data about entity UUID and birthtime to the table
				birthdate = server.getTicks();
				entity_birth_table.entityLifeTimeTable.put(entity.getUuid(), birthdate);
				//System.out.println("A Creeper has spawned with UUID:" + entity_birth_table.getMap());
				//System.out.println("A Creeper has spawned with UUID:" + entity.getUuidAsString() + "at " + birthdate);
				entity_birth_table.markDirty();
			}

		}

	}
}