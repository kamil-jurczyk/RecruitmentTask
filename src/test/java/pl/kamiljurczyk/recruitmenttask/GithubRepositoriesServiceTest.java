package pl.kamiljurczyk.recruitmenttask;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.kamiljurczyk.recruitmenttask.dto.NotForkRepositoriesResponse;
import pl.kamiljurczyk.recruitmenttask.exception.UserNotFoundException;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@WireMockTest(httpPort = 8081)
class GithubRepositoriesServiceTest {

    public static final String REPOSITORIES_URL_PATTERN = "/users/[^/]+/repos";
    private static final String REPOSITORIES_RESPONSE_JSON_PATH = "/__files/repositories-response.json";
    private static final String BRANCHES_RESPONSE_JSON_PATH = "/__files/branches-response.json";
    private static final String BAD_REQUEST_JSON_PATH = "/__files/bad-request.json";
    public static final String ACCEPT_CONTENT_TYPE = "application/json";
    public static final String TEST_NOTFOUND_USERNAME = "test-notfound-username";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String COMMIT_SHA = "cc6e5474ac20e186a1a56f0b48ec522b8c14818e";
    public static final String MASTER_BRANCH = "master";
    public static final String REPOSITORY_NAME = "kindle-clippings";
    public static final String USERNAME = "kamil-jurczyk";
    public static final String BRANCHES_URL_PATTERN = "/repos/[^/]+/[^/]+/branches";

    @Autowired
    private GithubRepositoriesService githubRepositoriesService;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("github.url", () -> "http://localhost:8081");
    }

    @Test
    void shouldReturnRepositoriesWithBranchesForUser() throws IOException {
        // given
        String repositoriesResponseJson = IOUtils.resourceToString(REPOSITORIES_RESPONSE_JSON_PATH, StandardCharsets.UTF_8);
        String branchesResponseJson = IOUtils.resourceToString(BRANCHES_RESPONSE_JSON_PATH, StandardCharsets.UTF_8);

        stubFor(get(urlPathMatching(REPOSITORIES_URL_PATTERN))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, ACCEPT_CONTENT_TYPE)
                                .withBody(repositoriesResponseJson)
                ));

        stubFor(get(urlPathMatching(BRANCHES_URL_PATTERN))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader(CONTENT_TYPE, ACCEPT_CONTENT_TYPE)
                                .withBody(branchesResponseJson)
                ));
        // when

        List<NotForkRepositoriesResponse> notForksRepositories =
                githubRepositoriesService.getNotForksRepositories(ACCEPT_CONTENT_TYPE, USERNAME);

        NotForkRepositoriesResponse repositoriesResponse = notForksRepositories.getFirst();
        // then
        assertThat(notForksRepositories).hasSize(1);
        assertThat(repositoriesResponse.ownerLogin()).isEqualTo(USERNAME);
        assertThat(repositoriesResponse.repositoryName()).isEqualTo(REPOSITORY_NAME);
        assertThat(repositoriesResponse.branchList().getFirst().getName()).isEqualTo(MASTER_BRANCH);
        assertThat(repositoriesResponse.branchList().getFirst().getCommit()).isEqualTo(COMMIT_SHA);
    }

    @Test
    void shouldReturnUserNotFoundException() throws IOException {
        // given
        String repositoriesResponseJson = IOUtils.resourceToString(BAD_REQUEST_JSON_PATH, StandardCharsets.UTF_8);

        stubFor(get(urlPathMatching(REPOSITORIES_URL_PATTERN))
                .willReturn(
                        aResponse()
                                .withStatus(404)
                                .withHeader(CONTENT_TYPE, ACCEPT_CONTENT_TYPE)
                                .withBody(repositoriesResponseJson)
                ));

        // then
        assertThatThrownBy(() -> githubRepositoriesService.getNotForksRepositories(ACCEPT_CONTENT_TYPE, TEST_NOTFOUND_USERNAME))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}