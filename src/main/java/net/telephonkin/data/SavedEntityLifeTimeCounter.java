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

public class SavedEntityLifeTimeCounter extends PersistentState {
    public long savedEntityLifeTimeCounter;

    public long getValue() {
        return this.savedEntityLifeTimeCounter;
    }

    public void setValue(long input_value) {
        this.savedEntityLifeTimeCounter = input_value;
        //System.out.println("input: " + this.entityLifeTimeTable);
        //System.out.println("Table: " + this.entityLifeTimeTable);
    }

    // --- SAVE LOGIC ---
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();

        NbtCompound entryCompound = new NbtCompound();
        entryCompound.putLong("savedEntityLifeTimeCounter", savedEntityLifeTimeCounter);

        /*for (Map.Entry<UUID, HashMap<String, Long>> entry : savedEntityLifeTimeCounter.entrySet()) {
            NbtCompound entryCompound = new NbtCompound();
            entryCompound.putUuid("UUID", entry.getKey());

            // Create a sub-compound to hold all the String-Long pairs for this UUID
            NbtCompound innerDataCompound = new NbtCompound();
            for (Map.Entry<String, Long> innerEntry : entry.getValue().entrySet()) {
                innerDataCompound.putLong(innerEntry.getKey(), innerEntry.getValue());
            }

            entryCompound.put("Values", innerDataCompound);
            list.add(entryCompound);
        }*/

        nbt.put("DataList", list);
        return nbt;
    }

    // --- LOAD LOGIC ---
    public static SavedEntityLifeTimeCounter fromNbt(NbtCompound nbt) {
        SavedEntityLifeTimeCounter state = new SavedEntityLifeTimeCounter();
        NbtList list = nbt.getList("DataList", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < list.size(); i++) {
            NbtCompound entryCompound = list.getCompound(i);

            /*// Read the sub-compound back into a HashMap
            NbtCompound innerDataCompound = entryCompound.getCompound("Values");
            HashMap<String, Long> innerMap = new HashMap<>();

            for (String key : innerDataCompound.getKeys()) {
                long value = innerDataCompound.getLong(key);
                innerMap.put(key, value);
            }*/

            state.savedEntityLifeTimeCounter = entryCompound.getLong("Long");
        }

        return state;
    }

    // --- GETTER FOR WORLD ---
    public static SavedEntityLifeTimeCounter get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                SavedEntityLifeTimeCounter::fromNbt,
                SavedEntityLifeTimeCounter::new,
                "saved_entity_life_time_counter"
        );
    }
}