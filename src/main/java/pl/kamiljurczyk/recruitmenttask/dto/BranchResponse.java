package pl.kamiljurczyk.recruitmenttask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class BranchResponse {

    private String name;
    private String commit;

    @JsonProperty("commit")
    private void extractCommitSha(Map<String, String> commit) {
        this.commit = commit.get("sha");
    }
}
