package pl.kamiljurczyk.recruitmenttask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@ToString
public class RepositoryResponse {
    @JsonProperty("name")
    private String repositoryName;
    private String owner;
    private Boolean fork;

    @JsonProperty("owner")
    private void extractOwnerLogin(Map<String, String> owner) {
        this.owner = owner.get("login");
    }
}
