package net.telephonkin;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.telephonkin.data.EntityLifeTimeTable;

public class EntityDespawner {
    public void loadedChunksDespawner(
            //MinecraftServer server,
            ServerWorld world,
            Entity entity
    ) {
        EntityLifeTimeTable entity_birth_table = EntityLifeTimeTable.get(world);
        int pos_x = entity.getChunkPos().x;
        int pos_z = entity.getChunkPos().z;
        // Check that the entity is in loaded chunk
        if (!entity.getWorld().isClient()) {
            if (world.isChunkLoaded(pos_x, pos_z)) {
                entity.discard(); // It will only despawn an entity
            }
        }
    }
}
