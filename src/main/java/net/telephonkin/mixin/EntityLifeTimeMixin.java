package net.telephonkin.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.telephonkin.data.EntityLifeTimeTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

			if (entity.getType() == EntityType.SLIME) {
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