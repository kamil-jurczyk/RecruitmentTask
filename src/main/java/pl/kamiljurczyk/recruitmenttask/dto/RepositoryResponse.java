package pl.kamiljurczyk.recruitmenttask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RepositoryResponse(
        @JsonProperty("name") String repositoryName,
        Owner owner,
        Boolean fork) {
}