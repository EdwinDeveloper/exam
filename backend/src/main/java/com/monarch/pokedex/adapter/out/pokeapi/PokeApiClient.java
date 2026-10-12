package com.monarch.pokedex.adapter.out.pokeapi;

import com.monarch.pokedex.application.port.PokeApiGateway;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.*;

@Component
public class PokeApiClient implements PokeApiGateway {
    private final RestClient client = RestClient.builder().baseUrl("https://pokeapi.co/api/v2").build();

    @Override
    public Map<String, Object> browse(int offset, int limit) {
        Map<String, Object> page = client.get().uri("/pokemon?offset={offset}&limit={limit}", offset, limit)
                .retrieve().body(Map.class);
        List<Map<String, Object>> results = list(page.get("results"));
        List<Map<String, Object>> cards = results.stream().map(item -> card((String) item.get("name"))).toList();
        return Map.of("count", page.getOrDefault("count", 0), "next", page.get("next") == null ? "" : page.get("next"),
                "previous", page.get("previous") == null ? "" : page.get("previous"), "results", cards);
    }

    @Override
    @Cacheable("pokemon-details")
    public Map<String, Object> details(String name) {
        Map<String, Object> pokemon = get("/pokemon/" + name.toLowerCase(Locale.ROOT));
        Map<String, Object> species = get("/pokemon-species/" + name.toLowerCase(Locale.ROOT));
        Map<String, Object> chain = Map.of();
        Map<String, Object> evolution = map(species.get("evolution_chain"));
        if (evolution.get("url") instanceof String url) {
            chain = RestClient.create().get().uri(url).retrieve().body(Map.class);
        }
        List<String> lineage = new ArrayList<>();
        addLineage(map(chain.get("chain")), lineage);
        Map<String, Object> view = cardFrom(pokemon);
        view.put("stats", list(pokemon.get("stats")).stream().map(stat -> Map.of(
                "name", map(stat.get("stat")).getOrDefault("name", ""), "value", stat.getOrDefault("base_stat", 0))).toList());
        view.put("description", list(species.get("flavor_text_entries")).stream()
                .filter(item -> "en".equals(map(item.get("language")).get("name"))).map(item -> item.get("flavor_text"))
                .findFirst().orElse("No field notes have been recorded."));
        view.put("evolution", lineage);
        view.put("species", map(species.get("genera")).isEmpty() ? "" : list(species.get("genera")).stream()
                .filter(item -> "en".equals(map(item.get("language")).get("name"))).map(item -> item.get("genus"))
                .findFirst().orElse(""));
        return view;
    }

    @Override
    public List<Map<String, Object>> localize(List<String> names) {
        return names.stream().map(this::card).toList();
    }

    private Map<String, Object> card(String name) { return cardFrom(get("/pokemon/" + name)); }

    private Map<String, Object> cardFrom(Map<String, Object> pokemon) {
        Map<String, Object> sprites = map(pokemon.get("sprites"));
        String sprite = Objects.toString(sprites.get("front_default"), "");
        List<String> types = list(pokemon.get("types")).stream().map(item -> Objects.toString(map(item.get("type")).get("name"), "")).toList();
        List<String> abilities = list(pokemon.get("abilities")).stream().map(item -> Objects.toString(map(item.get("ability")).get("name"), "")).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", pokemon.get("id")); result.put("name", pokemon.get("name"));
        result.put("category", String.join(" / ", types)); result.put("height", pokemon.get("height"));
        result.put("weight", pokemon.get("weight")); result.put("sprite", sprite); result.put("abilities", abilities);
        return result;
    }

    private Map<String, Object> get(String path) { return client.get().uri(path).retrieve().body(Map.class); }
    private void addLineage(Map<String, Object> node, List<String> output) {
        if (node.isEmpty()) return;
        List<Map<String, Object>> species = list(node.get("species"));
        if (!species.isEmpty()) output.add(Objects.toString(species.get(0).get("name"), ""));
        List<Map<String, Object>> next = list(node.get("evolves_to"));
        if (!next.isEmpty()) addLineage(next.get(0), output);
    }
    @SuppressWarnings("unchecked") private Map<String, Object> map(Object value) { return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of(); }
    @SuppressWarnings("unchecked") private List<Map<String, Object>> list(Object value) { return value instanceof List<?> l ? (List<Map<String, Object>>) l : List.of(); }
}
