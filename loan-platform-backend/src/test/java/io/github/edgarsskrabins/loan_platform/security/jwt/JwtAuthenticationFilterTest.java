package io.github.edgarsskrabins.loan_platform.security.jwt;

import io.github.edgarsskrabins.loan_platform.exceptions.UserNotFoundException;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String EMAIL = "ada@example.com";
    private static final String TOKEN = "a.valid.token";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("a request with no Authorization header passes straight through unauthenticated")
    void missingHeaderIsIgnored() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userService);
    }

    @Test
    @DisplayName("a non-Bearer Authorization header is ignored")
    void nonBearerHeaderIsIgnored() throws Exception {
        request.addHeader("Authorization", "Basic YWRhOnNlY3JldA==");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userService);
    }

    @Test
    @DisplayName("a valid Bearer token authenticates the request and continues the chain")
    void validTokenAuthenticatesRequest() throws Exception {
        User user = user();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(user);
        assertThat(authentication.isAuthenticated()).isTrue();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("the authentication carries no authorities, so role-based rules cannot match")
    void authenticationCarriesNoAuthorities() throws Exception {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userService.getUserByEmail(EMAIL)).thenReturn(user());

        filter.doFilterInternal(request, response, filterChain);

        // Documents current behaviour: the filter passes List.of() as authorities, so
        // hasRole(...) / @PreAuthorize can never succeed and every role check has to be
        // hand-written inside the services.
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("an already-authenticated context is not overwritten")
    void existingAuthenticationIsPreserved() throws Exception {
        Authentication existing = new UsernamePasswordAuthenticationToken("someone-else", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(userService, never()).getUserByEmail(EMAIL);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("a token for a deleted account propagates UserNotFoundException")
    void unknownUserPropagates() {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userService.getUserByEmail(EMAIL)).thenThrow(new UserNotFoundException(EMAIL));

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(UserNotFoundException.class);
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("an invalid token escapes the filter as a JwtException")
    void invalidTokenEscapesTheFilter() {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenThrow(new JwtException("bad signature"));

        // Documents current behaviour, see the @Disabled spec below.
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(JwtException.class);
        verifyNoInteractions(filterChain);
    }

    @Test
    @Disabled("""
            BUG: extractUsername throws on an expired/forged/malformed token and the filter does
            not catch it, so a bad token surfaces as HTTP 500 instead of 401. Expected behaviour is
            to leave the context unauthenticated and continue the chain, letting the entry point
            return 401. Remove @Disabled once the parse is wrapped in a try/catch.""")
    @DisplayName("an invalid token leaves the request unauthenticated and continues the chain")
    void invalidTokenIsTreatedAsUnauthenticated() throws Exception {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenThrow(new JwtException("bad signature"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private static User user() {
        User user = new User();
        user.setId(7L);
        user.setEmail(EMAIL);
        user.setPasswordHash("hashed");
        user.setRole(Role.CUSTOMER);
        return user;
    }
}
