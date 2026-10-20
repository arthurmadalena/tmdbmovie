package io.github.arthurmadalena.moviedatabaseapi;

import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/tmdb")
public class TmdbController {

    private final TmdbService service;

    public TmdbController(TmdbService service) { this.service = service; }

    @GetMapping("/filmes")
    public JsonNode buscar(@RequestParam String q) { return service.buscarFilmes(q); }

    @GetMapping("/filmes/{id}")
    public JsonNode detalhes(@PathVariable long id) { return service.detalhesFilme(id); }

    @PostMapping("/listas")
    public JsonNode criarLista(@RequestHeader("X-User-Token") String token,
                               @RequestBody Map<String, String> body) {
        return service.criarLista(token, body.get("nome"), body.get("descricao"));
    }

    @PostMapping("/listas/{listId}/filmes/{movieId}")
    public JsonNode adicionar(@RequestHeader("X-User-Token") String token,
                              @PathVariable long listId, @PathVariable long movieId) {
        return service.adicionarFilme(token, listId, movieId);
    }

    @PutMapping("/listas/{listId}")
    public JsonNode atualizar(@RequestHeader("X-User-Token") String token,
                              @PathVariable long listId,
                              @RequestBody Map<String, String> body) {
        return service.atualizarLista(token, listId, body.get("nome"), body.get("descricao"));
    }

    @DeleteMapping("/listas/{listId}/filmes/{movieId}")
    public JsonNode remover(@RequestHeader("X-User-Token") String token,
                            @PathVariable long listId, @PathVariable long movieId) {
        return service.removerFilme(token, listId, movieId);
    }

    @DeleteMapping("/listas/{listId}")
    public JsonNode apagar(@RequestHeader("X-User-Token") String token,
                           @PathVariable long listId) {
        return service.apagarLista(token, listId);
    }
}
