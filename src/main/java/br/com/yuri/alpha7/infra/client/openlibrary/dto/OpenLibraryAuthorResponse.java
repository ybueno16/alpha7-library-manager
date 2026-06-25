package br.com.yuri.alpha7.infra.client.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

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
