package net.telephonkin.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ToDespawnEntityCacheHashSet extends PersistentState {
    // Stores cache about entities that should be deleted, but they cannot because they are not in the loaded chunks
    private final Set<UUID> toDespawnEntityCache = new HashSet<>();

    public Set<UUID> getSet() {
        return this.toDespawnEntityCache;
    }

    public void setSet(Set<UUID> inputSet) {
        this.toDespawnEntityCache.clear();
        if (inputSet != null) {
            this.toDespawnEntityCache.addAll(inputSet);
        }
    }

    public void addUUID(UUID input_uuid) {
        this.toDespawnEntityCache.add(input_uuid
        );
    }

    public void removeUUID (UUID input_uuid) {
        this.toDespawnEntityCache.remove(input_uuid);
    }

    // --- SAVE LOGIC ---
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();

        for (UUID uuid : toDespawnEntityCache) {
            NbtCompound entryCompound = new NbtCompound();
            entryCompound.putUuid("UUID", uuid);
            list.add(entryCompound);
        }

        nbt.put("DataList", list);
        return nbt;
    }

    // --- LOAD LOGIC ---
    public static ToDespawnEntityCacheHashSet fromNbt(NbtCompound nbt) {
        ToDespawnEntityCacheHashSet state = new ToDespawnEntityCacheHashSet();
        NbtList list = nbt.getList("DataList", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < list.size(); i++) {
            NbtCompound entryCompound = list.getCompound(i);
            UUID uuid = entryCompound.getUuid("UUID");
            state.toDespawnEntityCache.add(uuid);
        }

        return state;
    }

    // --- GETTER FOR WORLD ---
    public static ToDespawnEntityCacheHashSet get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                ToDespawnEntityCacheHashSet::fromNbt,
                ToDespawnEntityCacheHashSet::new,
                "to_despawn_entity_cache"
        );
    }
}

