package com.monarch.pokedex.application.usecase;

public class DuplicatePokemonException extends RuntimeException {
    public DuplicatePokemonException(String name) {
        super("A local record for " + name + " already exists");
    }
}
