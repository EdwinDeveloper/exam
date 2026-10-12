package com.monarch.pokedex.application.usecase;

import com.monarch.pokedex.application.port.PokemonStore;
import com.monarch.pokedex.domain.LocalPokemon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonUseCaseTest {
    private static final Long POKEMON_ID = 25L;
    private static final String POKEMON_NAME = "pikachu"; // Stable fixture identifying the local record.

    @Mock
    private PokemonStore store;

    @InjectMocks
    private PokemonUseCase useCase;

    @Test
    void listLocalReturnsStoreRecords() {
        List<LocalPokemon> expected = List.of(pokemon(POKEMON_ID));
        when(store.findAll()).thenReturn(expected);

        assertSame(expected, useCase.listLocal());
    }

    @Test
    void getLocalReturnsMatchingRecord() {
        LocalPokemon expected = pokemon(POKEMON_ID);
        when(store.findById(POKEMON_ID)).thenReturn(Optional.of(expected));

        assertSame(expected, useCase.getLocal(POKEMON_ID));
    }

    @Test
    void getLocalThrowsWhenRecordDoesNotExist() {
        when(store.findById(POKEMON_ID)).thenReturn(Optional.empty());

        PokemonNotFoundException exception = assertThrows(PokemonNotFoundException.class,
                () -> useCase.getLocal(POKEMON_ID));

        assertEquals("Pokemon record " + POKEMON_ID + " was not found", exception.getMessage());
    }

    @Test
    void saveAllowsSameRecordToKeepItsName() {
        LocalPokemon expected = pokemon(POKEMON_ID);
        when(store.findByName(POKEMON_NAME)).thenReturn(Optional.of(expected));
        when(store.save(expected)).thenReturn(expected);

        assertSame(expected, useCase.save(expected));
        verify(store).save(expected);
    }

    @Test
    void saveRejectsNameOwnedByDifferentRecord() {
        LocalPokemon existing = pokemon(POKEMON_ID);
        LocalPokemon incoming = pokemon(POKEMON_ID + 1);
        when(store.findByName(POKEMON_NAME)).thenReturn(Optional.of(existing));

        assertThrows(DuplicatePokemonException.class, () -> useCase.save(incoming));
        verify(store, never()).save(incoming);
    }

    @Test
    void deleteRemovesExistingRecord() {
        when(store.findById(POKEMON_ID)).thenReturn(Optional.of(pokemon(POKEMON_ID)));

        useCase.delete(POKEMON_ID);

        verify(store).deleteById(POKEMON_ID);
    }

    @Test
    void deleteDoesNotRemoveMissingRecord() {
        when(store.findById(POKEMON_ID)).thenReturn(Optional.empty());

        assertThrows(PokemonNotFoundException.class, () -> useCase.delete(POKEMON_ID));
        verify(store, never()).deleteById(POKEMON_ID);
    }

    private static LocalPokemon pokemon(Long id) {
        return new LocalPokemon(id, POKEMON_NAME, "electric", 4, 60, "sprite", List.of(), "Kanto", "Mouse", "notes");
    }
}