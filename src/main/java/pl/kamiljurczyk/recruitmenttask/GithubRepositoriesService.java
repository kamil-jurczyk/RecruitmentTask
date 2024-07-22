package pl.kamiljurczyk.recruitmenttask;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.kamiljurczyk.recruitmenttask.dto.BranchResponse;
import pl.kamiljurczyk.recruitmenttask.dto.NotForkRepositoriesResponse;
import pl.kamiljurczyk.recruitmenttask.dto.RepositoryResponse;
import pl.kamiljurczyk.recruitmenttask.exception.InvalidRepositoryResponseException;
import pl.kamiljurczyk.recruitmenttask.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
class GithubRepositoriesService {

    private static final String ACCEPT_HEADER = "Accept";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REPOSITORY_ENDPOINT = "/users/{username}/repos";
    private static final String BRANCHES_ENDPOINT = "/repos/{owner}/{repo}/branches";
    private static final String GITHUB_URL = "https://api.github.com";

    @Value("${github.token}")
    private String githubToken;

    private final RestClient restClient = RestClient.create(GITHUB_URL);

    private final ObjectMapper objectMapper;

    List<NotForkRepositoriesResponse> getNotForksRepositories(String accept, String username) {
        ArrayList<NotForkRepositoriesResponse> notForkRepositories = new ArrayList<>();

        List<RepositoryResponse> repos = getGithubRepositories(accept, username);

        repos.stream()
                .filter(not(RepositoryResponse::fork))
                .forEach(repo -> {
                    String owner = repo.owner().login();
                    String repositoryName = repo.repositoryName();

                    notForkRepositories.add(NotForkRepositoriesResponse.builder()
                            .repositoryName(repositoryName)
                            .ownerLogin(owner)
                            .branchList(getGithubBranches(accept, owner, repositoryName))
                            .build());
                });

        return notForkRepositories;
    }

    private List<RepositoryResponse> getGithubRepositories(String accept, String username) {
        return restClient.get()
                .uri(REPOSITORY_ENDPOINT, username)
                .header(ACCEPT_HEADER, accept)
                .header(AUTHORIZATION_HEADER, githubToken)
                .exchange((clientRequest, clientResponse) -> {
                            if (clientResponse.getStatusCode().is4xxClientError()) {
                                throw new UserNotFoundException("User not found");
                            } else if (clientResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200))) {
                                return Arrays.asList(objectMapper.readValue(clientResponse.getBody(), RepositoryResponse[].class));
                            } else {
                                throw new InvalidRepositoryResponseException();
                            }
                        }
                );
    }

    private List<BranchResponse> getGithubBranches(String accept, String owner, String repo) {
        return restClient.get()
                .uri(BRANCHES_ENDPOINT, owner, repo)
                .header(ACCEPT_HEADER, accept)
                .header(AUTHORIZATION_HEADER, githubToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
