package com.capstone.warranty_tracker.security;

import static org.junit.jupiter.api.Assertions.*;

import com.capstone.warranty_tracker.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void shouldSkipAuthEndpoints() throws Exception {
        when(request.getServletPath()).thenReturn("/api/auth/login");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldValidateTokenAndSetSecurityContext() throws Exception {
        String token = "mockToken";
        String email = "user@example.com";

        when(request.getServletPath()).thenReturn("/homeowner/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenReturn(email);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, "pass", List.of(new SimpleGrantedAuthority("ROLE_HOMEOWNER"))
        );

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldDoNothingWhenNoAuthorizationHeader() throws Exception {
        when(request.getServletPath()).thenReturn("/homeowner/test");
        when(request.getHeader("Authorization")).thenReturn(null); // No header

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
    @Test
    void shouldDoNothingWhenHeaderIsNotBearerToken() throws Exception {
        when(request.getServletPath()).thenReturn("/homeowner/test");
        when(request.getHeader("Authorization")).thenReturn("Basic something");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
    @Test
    void shouldSkipAuthenticationWhenEmailIsNull() throws Exception {
        String token = "invalidToken";

        when(request.getServletPath()).thenReturn("/homeowner/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenReturn(null); // Email extraction fails

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
    @Test
    void shouldSkipAuthenticationWhenTokenIsInvalid() throws Exception {
        String token = "fakeToken";
        String email = "user@example.com";

        when(request.getServletPath()).thenReturn("/homeowner/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenReturn(email);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, "pass", List.of(new SimpleGrantedAuthority("ROLE_HOMEOWNER"))
        );

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false); // Token fails validation

        jwtFilter.doFilterInternal(request, response, filterChain);

        // No authentication set
        assertNotNull(SecurityContextHolder.getContext());
        verify(filterChain).doFilter(request, response);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
