package io.github.arthurmadalena.moviedatabaseapi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class TmdbService {

    private final RestClient v3;
    private final RestClient v4;

    public TmdbService(@Qualifier("tmdbV3") RestClient v3,
                       @Qualifier("tmdbV4") RestClient v4) {
        this.v3 = v3;
        this.v4 = v4;
    }

    private String bearer(String userToken) {
        return "Bearer " + userToken;
    }

    // ---------- AUTENTICAÇÃO v4 (para poder escrever) ----------

    // Passo 1: gera request token
    public JsonNode criarRequestToken(String redirectTo) {
        return v4.post().uri("/auth/request_token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("redirect_to", redirectTo))
                .retrieve().body(JsonNode.class);
    }
    // Passo 2: usuário abre https://www.themoviedb.org/auth/access?request_token=XXX e aprova

    // Passo 3: troca pelo access token do usuário (guarde o access_token e o account_id)
    public JsonNode criarAccessToken(String requestToken) {
        return v4.post().uri("/auth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("request_token", requestToken))
                .retrieve().body(JsonNode.class);
    }

    // ---------- GET ----------

    public JsonNode buscarFilmes(String query) {
        return v3.get()
                .uri(u -> u.path("/search/movie")
                        .queryParam("query", query)
                        .queryParam("language", "pt-BR")
                        .build())
                .retrieve().body(JsonNode.class);
    }

    public JsonNode detalhesFilme(long movieId) {
        return v3.get()
                .uri("/movie/{id}?language=pt-BR", movieId)
                .retrieve().body(JsonNode.class);
    }

    public JsonNode obterLista(long listId) {
        return v4.get().uri("/list/{id}", listId)
                .retrieve().body(JsonNode.class);
    }

    // ---------- POST: criar lista ----------

    public JsonNode criarLista(String userToken, String nome, String descricao) {
        return v4.post().uri("/list")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "name", nome,
                        "description", descricao,
                        "iso_639_1", "pt",
                        "iso_3166_1", "BR",
                        "public", true))
                .retrieve().body(JsonNode.class);
    }

    // ---------- POST: adicionar filme à lista ----------

    public JsonNode adicionarFilme(String userToken, long listId, long movieId) {
        return v4.post().uri("/list/{id}/items", listId)
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("items", List.of(
                        Map.of("media_type", "movie", "media_id", movieId))))
                .retrieve().body(JsonNode.class);
    }

    // ---------- PUT: atualizar lista ----------

    public JsonNode atualizarLista(String userToken, long listId, String nome, String descricao) {
        return v4.put().uri("/list/{id}", listId)
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", nome, "description", descricao))
                .retrieve().body(JsonNode.class);
    }

    // ---------- DELETE: remover filme da lista (DELETE com body) ----------

    public JsonNode removerFilme(String userToken, long listId, long movieId) {
        return v4.method(HttpMethod.DELETE).uri("/list/{id}/items", listId)
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("items", List.of(
                        Map.of("media_type", "movie", "media_id", movieId))))
                .retrieve().body(JsonNode.class);
    }

    // ---------- DELETE: apagar lista ----------

    public JsonNode apagarLista(String userToken, long listId) {
        return v4.delete().uri("/list/{id}", listId)
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .retrieve().body(JsonNode.class);
    }

    // ---------- Avaliações (v3, com guest session) ----------

    public JsonNode criarGuestSession() {
        return v3.get().uri("/authentication/guest_session/new")
                .retrieve().body(JsonNode.class);
    }

    // POST (e repetir o POST atualiza a nota)
    public JsonNode avaliar(long movieId, double nota, String guestSessionId) {
        return v3.post()
                .uri("/movie/{id}/rating?guest_session_id={s}", movieId, guestSessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("value", nota))
                .retrieve().body(JsonNode.class);
    }

    // DELETE
    public JsonNode removerAvaliacao(long movieId, String guestSessionId) {
        return v3.delete()
                .uri("/movie/{id}/rating?guest_session_id={s}", movieId, guestSessionId)
                .retrieve().body(JsonNode.class);
    }
}
