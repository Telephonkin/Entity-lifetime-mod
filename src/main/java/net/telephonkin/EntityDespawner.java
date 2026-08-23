package net.telephonkin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.telephonkin.data.ToDespawnEntityCacheHashSet;

public class EntityDespawner {
    public void loadedChunksDespawner(
            ServerWorld world,
            Entity entity
    ) {

        int pos_x = entity.getChunkPos().x;
        int pos_z = entity.getChunkPos().z;
        // Check that the entity is in loaded chunk
        if (!entity.getWorld().isClient()) {
            if (world.isChunkLoaded(pos_x, pos_z)) {
                entity.discard(); // It will only despawn an entity
            }
        } else {
            ToDespawnEntityCacheHashSet.get(world).addUUID(entity.getUuid());
            ToDespawnEntityCacheHashSet.get(world).markDirty();
        }
    }
}
