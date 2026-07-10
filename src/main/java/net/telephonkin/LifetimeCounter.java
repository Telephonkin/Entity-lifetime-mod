package net.telephonkin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.EntityLifeTimeTable;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifetimeCounter {
	@Final private MinecraftServer server;

	public void processStart() {
		ServerWorld world = server.getOverworld();
		EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
		long timer;
		long time_now = server.getTicks();
		Map.Entry<UUID, HashMap<String,Long>> first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
		Entity entity = world.getEntity(first_entity.getKey());

	}
}
