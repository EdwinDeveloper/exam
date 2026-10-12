package com.monarch.pokedex.adapter.in.web;

import com.monarch.pokedex.application.port.PokeApiGateway;
import com.monarch.pokedex.application.usecase.PokemonUseCase;
import com.monarch.pokedex.domain.LocalPokemon;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {
    private final PokemonUseCase useCase;
    private final PokeApiGateway pokeApi;

    public PokemonController(PokemonUseCase useCase, PokeApiGateway pokeApi) {
        this.useCase = useCase; this.pokeApi = pokeApi;
    }

    @GetMapping("/catalog")
    public Map<String, Object> browse(@RequestParam(defaultValue = "0") @PositiveOrZero int offset,
                                      @RequestParam(defaultValue = "24") int limit) {
        if (limit < 1 || limit > 60) throw new IllegalArgumentException("limit must be between 1 and 60");
        return pokeApi.browse(offset, limit);
    }

    @GetMapping("/catalog/{name}")
    public Map<String, Object> details(@PathVariable String name) { return pokeApi.details(name); }

    @GetMapping("/local")
    public List<LocalPokemon> listLocal() { return useCase.listLocal(); }

    @GetMapping("/local/{id}")
    public LocalPokemon getLocal(@PathVariable Long id) { return useCase.getLocal(id); }

    @PostMapping("/sync/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public LocalPokemon sync(@PathVariable String name) {
        Map<String, Object> remote = pokeApi.details(name);
        return useCase.save(new LocalPokemon(null, (String) remote.get("name"),
                (String) remote.get("category"), number(remote.get("height")), number(remote.get("weight")),
                (String) remote.get("sprite"), strings(remote.get("abilities")), null, null, null));
    }

    @PostMapping("/local")
    @ResponseStatus(HttpStatus.CREATED)
    public LocalPokemon create(@Valid @RequestBody PokemonWriteRequest request) {
        return useCase.save(request.toDomain(null));
    }

    @PutMapping("/local/{id}")
    public LocalPokemon update(@PathVariable Long id, @Valid @RequestBody PokemonWriteRequest request) {
        LocalPokemon current = useCase.getLocal(id);
        return useCase.save(request.toDomain(current.id(), current.name(), current.category(), current.height(),
                current.weight(), current.sprite(), current.abilities()));
    }

    @DeleteMapping("/local/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { useCase.delete(id); }

    private Integer number(Object value) { return value instanceof Number number ? number.intValue() : null; }
    @SuppressWarnings("unchecked") private List<String> strings(Object value) { return value instanceof List<?> values ? (List<String>) values : List.of(); }

    public record PokemonWriteRequest(@NotBlank String name, String category, @PositiveOrZero Integer height,
                                      @PositiveOrZero Integer weight, String sprite, List<String> abilities,
                                      String region, String classification, String notes) {
        LocalPokemon toDomain(Long id) {
            return new LocalPokemon(id, name.trim().toLowerCase(), category, height, weight, sprite,
                    abilities, region, classification, notes);
        }
        LocalPokemon toDomain(Long id, String existingName, String existingCategory, Integer existingHeight,
                              Integer existingWeight, String existingSprite, List<String> existingAbilities) {
            return new LocalPokemon(id, existingName, category == null ? existingCategory : category,
                    height == null ? existingHeight : height, weight == null ? existingWeight : weight,
                    sprite == null ? existingSprite : sprite, abilities == null ? existingAbilities : abilities,
                    region, classification, notes);
        }
    }
}
