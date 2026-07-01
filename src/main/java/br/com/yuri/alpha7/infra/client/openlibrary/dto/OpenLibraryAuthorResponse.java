package br.com.yuri.alpha7.infra.client.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * DTO que representa a resposta JSON do endpoint de autores da OpenLibrary API
 * (ex: {@code https://openlibrary.org/authors/OL23919A.json}).
 *
 * <p>Campos desconhecidos são ignorados via {@code @JsonIgnoreProperties} para que a classe
 * não precise ser atualizada a cada mudança no schema da API.
 *
 * <p>O campo {@code bio} é mapeado como {@link com.fasterxml.jackson.databind.JsonNode} porque
 * a API retorna tanto uma {@code String} simples quanto um objeto {@code {"type": ..., "value": ...}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryAuthorResponse {

    private String name;

    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("death_date")
    private String deathDate;

    private JsonNode bio;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getDeathDate() { return deathDate; }
    public void setDeathDate(String deathDate) { this.deathDate = deathDate; }

    public JsonNode getBio() { return bio; }
    public void setBio(JsonNode bio) { this.bio = bio; }
}
