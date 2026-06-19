package net.telephonkin.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


public class EntityLifeTimeTable extends PersistentState{
    // table of entity's birthdates must be organized, so LinkedHashMap is used
    public final Map<UUID, Long> entityLifeTimeTable = new LinkedHashMap<>();

    public LinkedHashMap<UUID, Long> getMap() {
        return (LinkedHashMap<UUID, Long>) this.entityLifeTimeTable;
    }

    // --- SAVE LOGIC ---
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();

        for (Map.Entry<UUID, Long> entry : entityLifeTimeTable.entrySet()) {
            NbtCompound compound = new NbtCompound();
            compound.putUuid("UUID", entry.getKey());
            compound.putLong("Value", entry.getValue());
            list.add(compound);
        }

        nbt.put("DataList", list);
        return nbt;
    }

    // --- LOAD LOGIC ---
    public static EntityLifeTimeTable fromNbt(NbtCompound nbt) {
        EntityLifeTimeTable state = new EntityLifeTimeTable();
        NbtList list = nbt.getList("DataList", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < list.size(); i++) {
            NbtCompound compound = list.getCompound(i);
            UUID uuid = compound.getUuid("UUID");
            long value = compound.getLong("Value");
            state.entityLifeTimeTable.put(uuid, value);
        }

        return state;
    }

    // --- GETTER FOR WORLD ---
    public static EntityLifeTimeTable get(ServerWorld world) {
        // "your_modid_data" acts as the filename in the world's data folder
        return world.getPersistentStateManager().getOrCreate(
                EntityLifeTimeTable::fromNbt,
                EntityLifeTimeTable::new,
                "entity_life_time_data"
        );
    }
}
