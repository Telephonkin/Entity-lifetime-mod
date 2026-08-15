package net.telephonkin;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.telephonkin.data.EntityLifeTimeTable;
import net.telephonkin.data.ToDespawnEntityCacheHashSet;

import java.util.HashSet;
import java.util.UUID;

public class EntityDespawner {
    public void loadedChunksDespawner(
            //MinecraftServer server,
            ServerWorld world,
            Entity entity
    ) {
        EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
        ToDespawnEntityCacheHashSet toDespawnEntityCacheHashSet = ToDespawnEntityCacheHashSet.get(world);

        int pos_x = entity.getChunkPos().x;
        int pos_z = entity.getChunkPos().z;
        // Check that the entity is in loaded chunk
        if (!entity.getWorld().isClient()) {
            if (world.isChunkLoaded(pos_x, pos_z)) {
                System.out.println("entity got dispawned:" + entity.getType().toString() + " with UUID " + entity.getUuidAsString());
                entity.discard(); // It will only despawn an entity
            }
        } else {
            ToDespawnEntityCacheHashSet.get(world).addUUID(entity.getUuid());
            ToDespawnEntityCacheHashSet.get(world).markDirty();
        }
    }
}
