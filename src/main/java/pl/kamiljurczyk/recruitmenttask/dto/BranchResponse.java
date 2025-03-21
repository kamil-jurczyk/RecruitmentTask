package pl.kamiljurczyk.recruitmenttask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BranchResponse(
        String name,
        Commit commit) {
}
