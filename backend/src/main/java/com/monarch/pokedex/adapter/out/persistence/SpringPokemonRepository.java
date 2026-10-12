package com.monarch.pokedex.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringPokemonRepository extends JpaRepository<PokemonEntity, Long> {
    Optional<PokemonEntity> findByNameIgnoreCase(String name);
}
