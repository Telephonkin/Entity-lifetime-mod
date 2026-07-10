package net.telephonkin.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class EntityLifeTimeTable extends PersistentState {
    public LinkedHashMap<UUID, HashMap<String, Long>> entityLifeTimeTable = new LinkedHashMap<>();

    public LinkedHashMap<UUID, HashMap<String, Long>> getMap() {
        return this.entityLifeTimeTable;
    }

    public void setMap(LinkedHashMap<UUID, HashMap<String, Long>> input_map) {
        this.entityLifeTimeTable = input_map;
        System.out.println("input: " + this.entityLifeTimeTable);
        System.out.println("Table: " + this.entityLifeTimeTable);
    }

    // --- SAVE LOGIC ---
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();

        for (Map.Entry<UUID, HashMap<String, Long>> entry : entityLifeTimeTable.entrySet()) {
            NbtCompound entryCompound = new NbtCompound();
            entryCompound.putUuid("UUID", entry.getKey());

            // Create a sub-compound to hold all the String-Long pairs for this UUID
            NbtCompound innerDataCompound = new NbtCompound();
            for (Map.Entry<String, Long> innerEntry : entry.getValue().entrySet()) {
                innerDataCompound.putLong(innerEntry.getKey(), innerEntry.getValue());
            }

            entryCompound.put("Values", innerDataCompound);
            list.add(entryCompound);
        }

        nbt.put("DataList", list);
        return nbt;
    }

    // --- LOAD LOGIC ---
    public static EntityLifeTimeTable fromNbt(NbtCompound nbt) {
        EntityLifeTimeTable state = new EntityLifeTimeTable();
        NbtList list = nbt.getList("DataList", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < list.size(); i++) {
            NbtCompound entryCompound = list.getCompound(i);
            UUID uuid = entryCompound.getUuid("UUID");

            // Read the sub-compound back into a HashMap
            NbtCompound innerDataCompound = entryCompound.getCompound("Values");
            HashMap<String, Long> innerMap = new HashMap<>();

            for (String key : innerDataCompound.getKeys()) {
                long value = innerDataCompound.getLong(key);
                innerMap.put(key, value);
            }

            state.entityLifeTimeTable.put(uuid, innerMap);
        }

        return state;
    }

    // --- GETTER FOR WORLD ---
    public static EntityLifeTimeTable get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                EntityLifeTimeTable::fromNbt,
                EntityLifeTimeTable::new,
                "entity_life_time_data"
        );
    }
}