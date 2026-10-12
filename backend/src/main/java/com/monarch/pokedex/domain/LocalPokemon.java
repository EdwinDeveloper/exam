package com.monarch.pokedex.domain;

import java.util.List;

public record LocalPokemon(Long id, String name, String category, Integer height, Integer weight,
                           String sprite, List<String> abilities, String region, String classification,
                           String notes) {
    public LocalPokemon {
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
    }
}
