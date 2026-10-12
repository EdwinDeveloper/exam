package com.monarch.pokedex.adapter.out.persistence;

import com.monarch.pokedex.domain.LocalPokemon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonPersistenceAdapterTest {
    private static final Long POKEMON_ID = 25L;
    private static final String POKEMON_NAME = "pikachu"; // Readable fixture data for persistence mapping.

    @Mock
    private SpringPokemonRepository repository;

    @InjectMocks
    private PokemonPersistenceAdapter adapter;

    @Test
    void findAllMapsEntitiesAndSplitsAbilities() {
        PokemonEntity entity = entity(POKEMON_ID, "static,lightning-rod");
        when(repository.findAll()).thenReturn(List.of(entity));

        LocalPokemon pokemon = adapter.findAll().getFirst();

        assertEquals(POKEMON_ID, pokemon.id());
        assertEquals(POKEMON_NAME, pokemon.name());
        assertEquals(List.of("static", "lightning-rod"), pokemon.abilities());
    }

    @Test
    void findByIdMapsEntityAndHandlesBlankAbilities() {
        when(repository.findById(POKEMON_ID)).thenReturn(Optional.of(entity(POKEMON_ID, " ")));

        LocalPokemon pokemon = adapter.findById(POKEMON_ID).orElseThrow();

        assertEquals(POKEMON_NAME, pokemon.name());
        assertEquals(List.of(), pokemon.abilities());
    }

    @Test
    void findByNameDelegatesCaseInsensitiveLookupAndMapsEntity() {
        when(repository.findByNameIgnoreCase(POKEMON_NAME)).thenReturn(Optional.of(entity(POKEMON_ID, null)));

        LocalPokemon pokemon = adapter.findByName(POKEMON_NAME).orElseThrow();

        assertEquals(POKEMON_ID, pokemon.id());
        assertEquals(List.of(), pokemon.abilities());
        verify(repository).findByNameIgnoreCase(POKEMON_NAME);
    }

    @Test
    void saveCreatesEntityAndJoinsAbilities() {
        LocalPokemon incoming = pokemon(null, List.of("static", "lightning-rod"));
        when(repository.save(any(PokemonEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon saved = adapter.save(incoming);

        ArgumentCaptor<PokemonEntity> captor = ArgumentCaptor.forClass(PokemonEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(POKEMON_NAME, captor.getValue().getName());
        assertEquals("static,lightning-rod", captor.getValue().getAbilities());
        assertEquals(incoming.name(), saved.name());
        assertEquals(incoming.abilities(), saved.abilities());
    }

    @Test
    void saveUpdatesExistingEntityFields() {
        PokemonEntity existing = entity(POKEMON_ID, "old-ability");
        LocalPokemon incoming = pokemon(POKEMON_ID, List.of("new-ability"));
        when(repository.findById(POKEMON_ID)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        LocalPokemon saved = adapter.save(incoming);

        assertEquals("water", existing.getCategory());
        assertEquals(12, existing.getHeight());
        assertEquals(300, existing.getWeight());
        assertEquals("new-ability", existing.getAbilities());
        assertEquals("Johto", existing.getRegion());
        assertEquals("Mouse", existing.getClassification());
        assertEquals("updated notes", existing.getNotes());
        assertEquals(List.of("new-ability"), saved.abilities());
        verify(repository).save(existing);
    }

    @Test
    void saveThrowsWhenUpdatingMissingEntity() {
        LocalPokemon incoming = pokemon(POKEMON_ID, List.of());
        when(repository.findById(POKEMON_ID)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> adapter.save(incoming));
    }

    @Test
    void deleteDelegatesToRepository() {
        adapter.deleteById(POKEMON_ID);

        verify(repository).deleteById(POKEMON_ID);
    }

    private static PokemonEntity entity(Long id, String abilities) {
        PokemonEntity entity = new PokemonEntity(POKEMON_NAME, "electric", 4, 60, "sprite", abilities,
                "Kanto", "Mouse", "notes");
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private static LocalPokemon pokemon(Long id, List<String> abilities) {
        return new LocalPokemon(id, POKEMON_NAME, "water", 12, 300, "updated sprite", abilities,
                "Johto", "Mouse", "updated notes");
    }
}