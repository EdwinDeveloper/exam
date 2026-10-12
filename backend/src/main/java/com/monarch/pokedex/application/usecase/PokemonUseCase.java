package com.monarch.pokedex.application.usecase;

import com.monarch.pokedex.application.port.PokemonStore;
import com.monarch.pokedex.domain.LocalPokemon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PokemonUseCase {
    private final PokemonStore store;

    public PokemonUseCase(PokemonStore store) {
        this.store = store;
    }

    public List<LocalPokemon> listLocal() {
        return store.findAll();
    }

    public LocalPokemon getLocal(Long id) {
        return store.findById(id).orElseThrow(() -> new PokemonNotFoundException(id));
    }

    @Transactional
    public LocalPokemon save(LocalPokemon pokemon) {
        store.findByName(pokemon.name()).filter(existing -> !existing.id().equals(pokemon.id()))
                .ifPresent(existing -> { throw new DuplicatePokemonException(pokemon.name()); });
        return store.save(pokemon);
    }

    @Transactional
    public void delete(Long id) {
        getLocal(id);
        store.deleteById(id);
    }
}
