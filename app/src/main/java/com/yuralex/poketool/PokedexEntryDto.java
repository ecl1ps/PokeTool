package com.yuralex.poketool;

import com.pokegoapi.api.pokemon.PokemonMeta;
import com.pokegoapi.api.pokemon.PokemonMetaRegistry;

import POGOProtos.Enums.PokemonIdOuterClass;

public class PokedexEntryDto {

    private final int id;
    private final int pokemonCount;
    private final int candy;

    private final PokemonIdOuterClass.PokemonId pokemonIdData;
    private final PokemonMeta metadata;

    public PokedexEntryDto(int id, int pokemonCount, int candy) {
        this.id = id;
        this.pokemonCount = pokemonCount;
        this.candy = candy;

        pokemonIdData = PokemonIdOuterClass.PokemonId.forNumber(id);
        metadata = PokemonMetaRegistry.getMeta(pokemonIdData);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return pokemonIdData.name();
    }

    public int getPokemonCount() {
        return pokemonCount;
    }

    public int getCandy() {
        return candy;
    }

    public int getCandyToEvolve() {
        return metadata.getCandyToEvolve();
    }

    public int getEvolutions() {
        if (getCandyToEvolve() == 0)
            return 0;

        return candy / getCandyToEvolve();
    }

    public int getPokemonForGrinder() {
        return getPokemonCount() - getEvolutions();
    }
}
