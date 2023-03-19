package com.polarbookshop.edgeservice.user;

import com.polarbookshop.edgeservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@WebFluxTest
@Import(SecurityConfig.class)
public class UserControllerTests {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    ReactiveClientRegistrationRepository reactiveClientRegistrationRepository;

    // test that unathenticated users are sent a 401 response
    @Test
    public void getUser_unauthenticated() {
        webTestClient.get().uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // test that authenticated users are sent a user object
    @Test
    public void getUser_authenticated() {
        var expectedUser = new User("username", "first", "last", List.of("employee", "customer"));

        webTestClient
                .mutateWith(configureMockOidcLogin(expectedUser))
                .get().uri("/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> assertThat(user).isEqualTo(expectedUser));
    }

    private SecurityMockServerConfigurers.OidcLoginMutator configureMockOidcLogin(User expectedUser) {
        return SecurityMockServerConfigurers.mockOidcLogin().idToken(token -> {
            token.claim(StandardClaimNames.PREFERRED_USERNAME, expectedUser.username());
            token.claim(StandardClaimNames.GIVEN_NAME, expectedUser.firstName());
            token.claim(StandardClaimNames.FAMILY_NAME, expectedUser.lastName());
        });
    }
}
