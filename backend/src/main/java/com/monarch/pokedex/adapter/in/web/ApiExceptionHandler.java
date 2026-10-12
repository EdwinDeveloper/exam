package com.monarch.pokedex.adapter.in.web;

import com.monarch.pokedex.application.usecase.DuplicatePokemonException;
import com.monarch.pokedex.application.usecase.PokemonNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PokemonNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(RuntimeException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", error.getMessage()));
    }
    @ExceptionHandler({DuplicatePokemonException.class, DataIntegrityViolationException.class})
    ResponseEntity<Map<String, String>> conflict(RuntimeException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error.getMessage()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    ResponseEntity<Map<String, String>> badRequest(Exception error) {
        String message = error instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getFieldErrors().stream().findFirst().map(field -> field.getField() + " " + field.getDefaultMessage()).orElse("Invalid request")
                : error.getMessage();
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
    @ExceptionHandler(org.springframework.web.client.RestClientException.class)
    ResponseEntity<Map<String, String>> upstream(RuntimeException error) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "PokeAPI is unavailable"));
    }
}
