package com.yuralex.poketool;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PokedexTests {

    @Before
    public void before() {

    }

    @Test
    public void entryRequiredCandy() throws Exception {
        PokedexEntryDto entry; //Spearow

        entry = new PokedexEntryDto(21, 0, 0);
        assertEquals(entry.getCandyToEvolve(), 50);
    }

    @Test
    public void entryEvolutionsSimple() throws Exception {
        PokedexEntryDto entry; //Spearow

        entry = new PokedexEntryDto(21, 0, 49);
        assertEquals(entry.getEvolutions(), 0);
        assertEquals(entry.getPokemonForGrinder(), 0);

        entry = new PokedexEntryDto(21, 0, 50);
        assertEquals(entry.getEvolutions(), 1);
        assertEquals(entry.getPokemonForGrinder(), -1);
    }

    @Test
    public void entryEvolutionsExtra() throws Exception {
        PokedexEntryDto entry; //Spearow

        entry = new PokedexEntryDto(21, 0, 50 * 49);
        assertEquals(entry.getEvolutions(), 49);
        assertEquals(entry.getEvolutionsExtra(), 0);
        assertEquals(entry.getPokemonForGrinder(), -49);

        entry = new PokedexEntryDto(21, 0, 50 * 49 + 1);
        assertEquals(entry.getEvolutions(), 49);
        assertEquals(entry.getEvolutionsExtra(), 1);
        assertEquals(entry.getPokemonForGrinder(), -50);
    }

    @Test
    public void entryEvolutionsExtraWithGrinded() throws Exception {
        PokedexEntryDto entry; //Spearow

        entry = new PokedexEntryDto(21, 50, 50 * 49);
        assertEquals(entry.getEvolutions(), 49);
        assertEquals(entry.getEvolutionsExtra(), 0);
        assertEquals(entry.getPokemonForGrinder(), 1);

        entry = new PokedexEntryDto(21, 51, 50 * 49);
        assertEquals(entry.getEvolutions(), 49);
        assertEquals(entry.getEvolutionsExtra(), 1);
        assertEquals(entry.getPokemonForGrinder(), 1);
    }
}