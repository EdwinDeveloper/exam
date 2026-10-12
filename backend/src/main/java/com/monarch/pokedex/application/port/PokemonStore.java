package com.monarch.pokedex.application.port;

import com.monarch.pokedex.domain.LocalPokemon;
import java.util.List;
import java.util.Optional;

public interface PokemonStore {
    List<LocalPokemon> findAll();
    Optional<LocalPokemon> findById(Long id);
    Optional<LocalPokemon> findByName(String name);
    LocalPokemon save(LocalPokemon pokemon);
    void deleteById(Long id);
}
