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
        return metadata != null ? metadata.getCandyToEvolve() : -1;
    }

    private int getCandyRemaining() {
        return getCandy() % getCandyToEvolve();
    }

    public int getEvolutions() {
        if (getCandyToEvolve() <= 0)
            return 0;

        return getCandy() / getCandyToEvolve();
    }

    public int getEvolutionsExtra() {
        if (getCandyToEvolve() <= 0)
            return 0;

        // +1 candy for each evolution
        int remainingCandy = getCandyRemaining() + getEvolutions();
        int extraEvolutions = remainingCandy / getCandyToEvolve();
        remainingCandy -= extraEvolutions * getCandyToEvolve();

        int totalPossibleEvolutions = getEvolutions() + extraEvolutions;
        int grindablePokemon = getPokemonCount() - totalPossibleEvolutions;
        if (grindablePokemon <= 0)
            return extraEvolutions;

        // +1 candy for every grinded pokemon
        while (grindablePokemon > 1) {
            remainingCandy++;
            grindablePokemon--;
            if (remainingCandy >= getCandyToEvolve()) {
                remainingCandy -= getCandyToEvolve();
                extraEvolutions++;
            }
        }

        return extraEvolutions;
    }

    public int getPokemonForGrinder() {
        return getPokemonCount() - getEvolutions() - getEvolutionsExtra();
    }
}
