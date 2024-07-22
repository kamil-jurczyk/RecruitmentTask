package pl.kamiljurczyk.recruitmenttask;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.kamiljurczyk.recruitmenttask.dto.BranchResponse;
import pl.kamiljurczyk.recruitmenttask.dto.NotForkRepositoriesResponse;
import pl.kamiljurczyk.recruitmenttask.dto.RepositoryResponse;
import pl.kamiljurczyk.recruitmenttask.exception.InvalidRepositoryResponseException;
import pl.kamiljurczyk.recruitmenttask.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
class GithubRepositoriesService {

    public static final String ACCEPT_HEADER = "Accept";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REPOSITORY_ENDPOINT = "/users/{username}/repos";
    public static final String BRANCHES_ENDPOINT = "/repos/{owner}/{repo}/branches";

    @Value("${github.url}")
    private String githubUrl;

    @Value("${github.token}")
    private String githubToken;

    private final RestClient restClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GithubRepositoriesService() {
        restClient = RestClient.builder()
                .build();
    }

    List<NotForkRepositoriesResponse> getNotForksRepositories(String accept, String username) {
        ArrayList<NotForkRepositoriesResponse> notForkRepositories = new ArrayList<>();

        RepositoryResponse[] repos = getGithubRepositories(accept, username);

        for (RepositoryResponse githubRepository : repos) {
            if (Boolean.TRUE.equals(githubRepository.getFork())) {
                continue;
            }

            notForkRepositories.add(NotForkRepositoriesResponse.builder()
                    .repositoryName(githubRepository.getRepositoryName())
                    .ownerLogin(githubRepository.getOwner())
                    .branchList(List.of(
                            getGithubBranches(accept, githubRepository.getOwner(), githubRepository.getRepositoryName())
                    ))
                    .build());
        }

        return notForkRepositories;
    }

    private RepositoryResponse[] getGithubRepositories(String accept, String username) {
        return restClient.get()
                .uri(githubUrl + REPOSITORY_ENDPOINT, username)
                .header(ACCEPT_HEADER, accept)
                .header(AUTHORIZATION_HEADER, githubToken)
                .exchange((clientRequest, clientResponse) -> {
                            if (clientResponse.getStatusCode().is4xxClientError()) {
                                throw new UserNotFoundException("User not found");
                            } else if (clientResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200))) {
                                return objectMapper.readValue(clientResponse.getBody(), RepositoryResponse[].class);
                            } else {
                                throw new InvalidRepositoryResponseException();
                            }
                        }
                );
    }

    private BranchResponse[] getGithubBranches(String accept, String owner, String repo) {
        return restClient.get()
                .uri(githubUrl + BRANCHES_ENDPOINT, owner, repo)
                .header(ACCEPT_HEADER, accept)
                .header(AUTHORIZATION_HEADER, githubToken)
                .retrieve()
                .body(BranchResponse[].class);
    }
}
