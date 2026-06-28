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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.BiConsumer;

@Mixin(ServerWorld.class)
public abstract class EntityLifeTimeMixin {

	@Shadow @Final private MinecraftServer server;

	@Unique
	private static HashMap<String, Integer> LOADED_MOD_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedConfig();

	private static LinkedHashMap<UUID, Long> putProperly(
            ServerWorld world,
			LinkedHashMap<UUID, Long> input_map,
            Entity entity,
            UUID to_put_uuid,
            Long to_put_birth_time)  {


		LinkedHashMap<UUID, Long> reversed_entity_list = new LinkedHashMap<UUID, Long>(input_map.reversed());
		LinkedHashMap<UUID, Long> output_reversed = new LinkedHashMap<UUID, Long>();

		final boolean[] is_new_entity_added = {false};
		reversed_entity_list.forEach((key, value) -> {
			String entity_type_from_map = Objects.requireNonNull(world.getEntity(key)).getType().toString();
			String new_entity_type = entity.getType().toString();
			int how_much_does_new_entity_live = LOADED_MOD_CONFIG.get(new_entity_type);
			int how_much_does_entity_from_map_live = LOADED_MOD_CONFIG.get(entity_type_from_map);

			if (is_new_entity_added[0]) {
				output_reversed.put(key, value);
			} else {
				if (value + how_much_does_entity_from_map_live > to_put_birth_time + how_much_does_new_entity_live) { // Means that new spawned entity will be despawned before than previous entity, so we need to place it before and check again
					output_reversed.put(key, value);
				} else { // Means that new spawned entity will be despawned after than previous entity, so put new entity to the end
					output_reversed.put(key, value);
					output_reversed.put(to_put_uuid, to_put_birth_time);
					is_new_entity_added[0] = true;

				}
			}
		});
        return new LinkedHashMap<UUID, Long>(output_reversed.reversed());
	}

	@Inject(method = "spawnEntity", at = @At("HEAD"))
	public void onEntitySpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		// Server-side logic, which represents entity natural spawn
		if (!entity.getWorld().isClient()) {

			ServerWorld world = server.getOverworld();
			EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
			long birthdate;


			String entityTypeString = Registries.ENTITY_TYPE.getId(entity.getType()).toString();

			if (LOADED_MOD_CONFIG.get(entityTypeString) != -1) {
				// write data about entity UUID and birthtime to the table
				birthdate = server.getTicks();

				entity_birth_table.setMap(putProperly(
						world,
						entity_birth_table.getMap(),
						entity,
						entity.getUuid(),
						birthdate));
				//entity_birth_table.entityLifeTimeTable.put(entity.getUuid(), birthdate);
				//System.out.println("A Creeper has spawned with UUID:" + entity_birth_table.getMap());
				//System.out.println("A Creeper has spawned with UUID:" + entity.getUuidAsString() + "at " + birthdate);
				entity_birth_table.markDirty();
			}

		}

	}
}