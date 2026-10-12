package com.monarch.pokedex.application.usecase;

public class PokemonNotFoundException extends RuntimeException {
    public PokemonNotFoundException(Long id) {
        super("Pokemon record " + id + " was not found");
    }
}
