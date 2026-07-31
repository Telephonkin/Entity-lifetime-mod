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
    }

    // --- SAVE LOGIC ---
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putLong("savedEntityLifeTimeCounter", this.savedEntityLifeTimeCounter);
        return nbt;
    }

    // --- LOAD LOGIC ---
    public static SavedEntityLifeTimeCounter fromNbt(NbtCompound nbt) {
        SavedEntityLifeTimeCounter state = new SavedEntityLifeTimeCounter();
        // Read using the exact same key string
        state.savedEntityLifeTimeCounter = nbt.getLong("savedEntityLifeTimeCounter");
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