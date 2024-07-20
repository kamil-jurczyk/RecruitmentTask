package pl.kamiljurczyk.recruitmenttask.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record NotForkRepositoriesResponse(String repositoryName,
                                          String ownerLogin,
                                          List<BranchResponse> branchList) {
}
