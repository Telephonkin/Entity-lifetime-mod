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

	// TODO make a EntityDespawner class which has 3 ways to despawn an entity: when player load chunk, when mod loads the chunk entity-to-despawn exist and the despawn, despawn through editing world data. Edit these modes via entity_lifetime_common_config.json5

	public void processStart() {
		ServerWorld world = server.getOverworld();
		EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
		long timer;
		long time_now = server.getTicks();
		Map.Entry<UUID, HashMap<String,Long>> first_entity = entity_birth_table.entityLifeTimeTable.entrySet().iterator().next();
		Entity entity = world.getEntity(first_entity.getKey());
	}
}
