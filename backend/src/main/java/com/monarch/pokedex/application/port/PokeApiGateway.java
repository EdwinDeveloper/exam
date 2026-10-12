package com.monarch.pokedex.application.port;

import java.util.List;
import java.util.Map;

public interface PokeApiGateway {
    Map<String, Object> browse(int offset, int limit);
    Map<String, Object> details(String name);
    List<Map<String, Object>> localize(List<String> names);
}
