package com.monarch.pokedex.config;

import com.monarch.pokedex.adapter.out.persistence.PokemonEntity;
import com.monarch.pokedex.adapter.out.persistence.SpringPokemonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Configuration
public class DemoData {
    @Bean CommandLineRunner seedDemoData(SpringPokemonRepository pokemon, UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (pokemon.count() == 0) pokemon.saveAll(List.of(
                    new PokemonEntity("bulbasaur", "grass / poison", 7, 69, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png", "overgrow,chlorophyll", "Kanto", "Starter", "A seedling catalogued by the field team."),
                    new PokemonEntity("charmander", "fire", 6, 85, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png", "blaze,solar-power", "Kanto", "Starter", "Its tail flame reflects its vitality."),
                    new PokemonEntity("squirtle", "water", 5, 90, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png", "torrent,rain-dish", "Kanto", "Starter", "A compact turtle with a protective shell."),
                    new PokemonEntity("pikachu", "electric", 4, 60, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png", "static,lightning-rod", "Kanto", "Field note", "Known to gather in groups near wooded areas."),
                    new PokemonEntity("eevee", "normal", 3, 65, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/133.png", "run-away,adaptability", "Kanto", "Research subject", "Its unstable genetic code enables varied evolution.")));
            if (!users.existsByUsername("trainer")) users.save(new UserEntity("trainer", encoder.encode("pokedex123")));
        };
    }
}
