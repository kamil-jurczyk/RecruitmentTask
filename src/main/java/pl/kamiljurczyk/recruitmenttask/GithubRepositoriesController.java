package pl.kamiljurczyk.recruitmenttask;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import pl.kamiljurczyk.recruitmenttask.dto.NotForkRepositoriesResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
class GithubRepositoriesController {

    private final GithubRepositoriesService githubRepositoriesService;

    @GetMapping("/users/{username}/repos")
    ResponseEntity<List<NotForkRepositoriesResponse>> getNotForksRepositories(@RequestHeader(HttpHeaders.ACCEPT) String accept,
                                                                              @PathVariable("username") String username) {
        return ResponseEntity.ok(githubRepositoriesService.getNotForksRepositories(accept, username));
    }
}
