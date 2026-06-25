package br.com.yuri.alpha7.infra.client.openlibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryBookResponse {

    private String title;
    private List<String> publishers = Collections.emptyList();

    @JsonProperty("publish_date")
    private String publishDate;

    @JsonProperty("number_of_pages")
    private Integer numberOfPages;

    private List<LanguageRef> languages = Collections.emptyList();
    private List<AuthorRef> authors = Collections.emptyList();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public List<LanguageRef> getLanguages() {
        return languages;
    }

    public void setLanguages(List<LanguageRef> languages) {
        this.languages = languages;
    }

    public List<AuthorRef> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorRef> authors) {
        this.authors = authors;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LanguageRef {

        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthorRef {

        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
