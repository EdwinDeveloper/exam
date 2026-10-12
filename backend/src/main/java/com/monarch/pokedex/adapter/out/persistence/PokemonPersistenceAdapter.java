package com.monarch.pokedex.adapter.out.persistence;

import com.monarch.pokedex.application.port.PokemonStore;
import com.monarch.pokedex.domain.LocalPokemon;
import org.springframework.stereotype.Repository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class PokemonPersistenceAdapter implements PokemonStore {
    private final SpringPokemonRepository repository;

    public PokemonPersistenceAdapter(SpringPokemonRepository repository) { this.repository = repository; }

    public List<LocalPokemon> findAll() { return repository.findAll().stream().map(this::toDomain).toList(); }
    public Optional<LocalPokemon> findById(Long id) { return repository.findById(id).map(this::toDomain); }
    public Optional<LocalPokemon> findByName(String name) { return repository.findByNameIgnoreCase(name).map(this::toDomain); }

    public LocalPokemon save(LocalPokemon pokemon) {
        PokemonEntity entity = pokemon.id() == null ? new PokemonEntity(pokemon.name(), pokemon.category(),
                pokemon.height(), pokemon.weight(), pokemon.sprite(), String.join(",", pokemon.abilities()),
                pokemon.region(), pokemon.classification(), pokemon.notes()) : repository.findById(pokemon.id())
                .map(existing -> update(existing, pokemon)).orElseThrow();
        return toDomain(repository.save(entity));
    }

    public void deleteById(Long id) { repository.deleteById(id); }

    private PokemonEntity update(PokemonEntity entity, LocalPokemon pokemon) {
        entity.setCategory(pokemon.category()); entity.setHeight(pokemon.height()); entity.setWeight(pokemon.weight());
        entity.setSprite(pokemon.sprite()); entity.setAbilities(String.join(",", pokemon.abilities()));
        entity.setRegion(pokemon.region()); entity.setClassification(pokemon.classification()); entity.setNotes(pokemon.notes());
        return entity;
    }

    private LocalPokemon toDomain(PokemonEntity entity) {
        List<String> abilities = entity.getAbilities() == null || entity.getAbilities().isBlank()
                ? List.of() : Arrays.asList(entity.getAbilities().split(","));
        return new LocalPokemon(entity.getId(), entity.getName(), entity.getCategory(), entity.getHeight(),
                entity.getWeight(), entity.getSprite(), abilities, entity.getRegion(), entity.getClassification(), entity.getNotes());
    }
}
