package net.telephonkin;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.telephonkin.data.ToDespawnEntityCacheHashSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;


public class EntityDespawner {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Entity Lifetime");

    @Unique
    private static HashMap<String, Object> LOADED_MOD_COMMON_CONFIG = EntityLifeTimeMod.INSTANCE.getLoadedCommonConfig();

    public void loadedChunksDespawner(
            MinecraftServer server,
            UUID entityUUID
    ) {
        AtomicReference<Entity> entity = new AtomicReference<>();
        server.getWorlds().forEach(world -> {
            if (world.getEntity(entityUUID) != null) {
                entity.set(world.getEntity(entityUUID));
                int pos_x = entity.get().getChunkPos().x;
                int pos_z = entity.get().getChunkPos().z;
                // Check that the entity is in loaded chunk
                if (!entity.get().getWorld().isClient()) {
                    if (world.isChunkLoaded(pos_x, pos_z)) {

                        // Common setting entity spawn logging
                        // Convert ArrayList to HashSet
                        List<String> logsConfigAsList = (ArrayList<String>) LOADED_MOD_COMMON_CONFIG.get("logs");
                        HashSet<String> logsConfig = new HashSet<String>(logsConfigAsList);
                        if (logsConfig.contains("despawn")) {
                            LOGGER.info("Despawned at time " + server.getOverworld().getTime() + " Entity with a type " + entity.get().getType().toString() + " and UUID " + entity.get().getUuid().toString());
                        }

                        entity.get().discard(); // It will only despawn an entity
                    }
                } else {
                    ToDespawnEntityCacheHashSet.get(world).addUUID(entityUUID);
                    ToDespawnEntityCacheHashSet.get(world).markDirty();
                }
            } else {
                ToDespawnEntityCacheHashSet.get(world).addUUID(entityUUID);
                ToDespawnEntityCacheHashSet.get(world).markDirty();
            }
        });
    }
}
