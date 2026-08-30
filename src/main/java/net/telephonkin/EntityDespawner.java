package net.telephonkin;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.telephonkin.data.ToDespawnEntityCacheHashSet;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class EntityDespawner {
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
