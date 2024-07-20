package pl.kamiljurczyk.recruitmenttask;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@EnableWireMock({
        @ConfigureWireMock(name = "githubRepository-service")
})
class GithubRepositoriesServiceTest {

    private static final String CORRECT_RESPONSE_JSON_PATH = "/__files/correct-response.json";
    private static final String BAD_REQUEST_JSON_PATH = "/__files/bad-request.json";

    @InjectWireMock("githubRepository-service")
    private WireMockServer wiremock;

    @Autowired
    private GithubRepositoriesService githubRepositoriesService;

    @Test
    void shouldReturnRepositoriesWithBranchesForUser() throws IOException {
        // given
        String response = IOUtils.resourceToString(CORRECT_RESPONSE_JSON_PATH, StandardCharsets.UTF_8);

        wiremock.stubFor(get(urlEqualTo("/"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(response)
                ));
        // when

        List<NotForkRepositoriesResponse> notForksRepositories =
                githubRepositoriesService.getNotForksRepositories("application/json", "kamil-jurczyk");

        NotForkRepositoriesResponse repositoriesResponse = notForksRepositories.getFirst();
        // then
        assertThat(notForksRepositories).hasSize(1);
        assertThat(repositoriesResponse.ownerLogin()).isEqualTo("kamil-jurczyk");
        assertThat(repositoriesResponse.repositoryName()).isEqualTo("kindle-clippings");
        assertThat(repositoriesResponse.branchList().getFirst().getName()).isEqualTo("master");
        assertThat(repositoriesResponse.branchList().getFirst().getCommit()).isEqualTo("cc6e5474ac20e186a1a56f0b48ec522b8c14818e");
    }

    @Test
    void shouldReturnUserNotFoundException() throws IOException {
        // given
        String response = IOUtils.resourceToString(BAD_REQUEST_JSON_PATH, StandardCharsets.UTF_8);

        wiremock.stubFor(get(urlEqualTo("/"))
                .willReturn(
                        aResponse()
                                .withStatus(404)
                                .withHeader("Content-Type", "application/json")
                                .withBody(response)
                ));
        // then
        assertThatThrownBy(() -> githubRepositoriesService.getNotForksRepositories("application/json", "test-notfound-username"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}