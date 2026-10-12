package com.monarch.pokedex.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalPokemonTest {
    @Test
    void nullAbilitiesAreNormalizedToAnEmptyList() {
        LocalPokemon pokemon = pokemon(null);

        assertEquals(List.of(), pokemon.abilities());
    }

    @Test
    void abilitiesAreDefensivelyCopied() {
        List<String> abilities = new ArrayList<>(List.of("static", "lightning-rod"));

        LocalPokemon pokemon = pokemon(abilities);
        abilities.add("surge-surf");

        assertEquals(List.of("static", "lightning-rod"), pokemon.abilities());
    }

    @Test
    void abilitiesCannotBeMutatedThroughAccessor() {
        LocalPokemon pokemon = pokemon(List.of("static"));

        assertThrows(UnsupportedOperationException.class, () -> pokemon.abilities().add("lightning-rod"));
    }

    private static LocalPokemon pokemon(List<String> abilities) {
        return new LocalPokemon(25L, "pikachu", "electric", 4, 60, "sprite", abilities,
                "Kanto", "Mouse", "notes");
    }
}