package com.yuralex.poketool;

import android.os.Parcel;
import android.os.Parcelable;

import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.pokemon.PokemonMeta;
import com.pokegoapi.api.pokemon.PokemonMetaRegistry;

import java.util.ArrayList;
import java.util.List;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;

public class PokedexDto implements Parcelable {

    private List<PokedexEntryDto> entries;

    public PokedexDto(PokeBank pokebank, CandyJar candyJar) {
        int entryCount = PokemonIdOuterClass.PokemonId.values().length - 2; // except 0 and -1
        entries = new ArrayList<>(entryCount);

        for (int pokedexId = 1; pokedexId < entryCount; ++pokedexId) {
            PokemonIdOuterClass.PokemonId pokemonId = PokemonIdOuterClass.PokemonId.forNumber(pokedexId);
            PokemonMeta meta = PokemonMetaRegistry.getMeta(pokemonId);

            PokedexEntryDto entry = new PokedexEntryDto(
                pokedexId,
                pokebank.getPokemonByPokemonId(pokemonId).size(),
                meta != null ? candyJar.getCandies(meta.getFamily()) : -1
            );

            entries.add(pokedexId - 1, entry);
        }
    }

    public PokedexEntryDto get(int pokedexId) {
        return entries.get(pokedexId - 1);
    }

    public int count() {
        return entries.size();
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PokedexDto> CREATOR = new Parcelable.Creator<PokedexDto>() {
        public PokedexDto createFromParcel(Parcel in) {
            return new PokedexDto(in);
        }

        public PokedexDto[] newArray(int size) {
            return new PokedexDto[size];
        }
    };

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(count());
        for (PokedexEntryDto entry: entries) {
            out.writeInt(entry.getId());
            out.writeInt(entry.getPokemonCount());
            out.writeInt(entry.getCandy());
        }
    }

    /** recreate object from parcel */
    private PokedexDto(Parcel in) {
        int entryCount = in.readInt();
        entries = new ArrayList<>(entryCount);

        for (int i = 0; i < entryCount; ++i) {
            PokedexEntryDto entry = new PokedexEntryDto(
                in.readInt(),
                in.readInt(),
                in.readInt()
            );

            entries.add(entry.getId() - 1, entry);
        }
    }
}
