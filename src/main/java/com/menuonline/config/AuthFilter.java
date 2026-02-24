package com.menuonline.config;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.menuonline.entity.TokenAccess;
import com.menuonline.repository.TokenAccessRepository;
import com.menuonline.utils.TokenAccessUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private static final UrlPathMethod[] FREE_URI = {
            new UrlPathMethod("/stripe/webhook", "POST"),
            new UrlPathMethod("/user", "POST"),
            new UrlPathMethod("/user/login", "POST"),
            new UrlPathMethod("/menu/*", "POST"),
            new UrlPathMethod("/menu/*", "GET")
    };

    private static final AntPathMatcher matcher = new AntPathMatcher();

    private static final String AUTH_HEADER = "x-mo-token";

    public static final String USER_ATTR_KEY = "USER_ATTR_KEY";
    public static final String TOKEN_ATTR_KEY = "TOKEN_ATTR_KEY";

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private final TokenAccessRepository tokenAccessRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(AUTH_HEADER);
        log.info("doFilterInternal - authHeader:{}", authHeader);

        if (authHeader == null) {
            log.warn("doFilterInternal - no token header");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authorization header not found!");
            return;
        }

        Optional<TokenAccess> tokenOpt = tokenAccessRepository.findById(authHeader);

        if (tokenOpt.isEmpty()) {
            log.warn("doFilterInternal - token not found:{}", authHeader);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token not found!");
            return;
        }

        TokenAccess tokenAccess = tokenOpt.get();

        if (!TokenAccessUtil.validate(tokenAccess)) {
            log.warn("doFilterInternal - token expired:{}", tokenAccess.getToken());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token expired!");
            return;
        }

        request.setAttribute(USER_ATTR_KEY, tokenAccess.getUser());
        request.setAttribute(TOKEN_ATTR_KEY, tokenAccess.getToken());

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (HttpMethod.OPTIONS.matches(method))
            return true;

        for (UrlPathMethod upm : FREE_URI) {
            boolean match = matcher.match(upm.url, uri);
            boolean methodMatch = upm.method().equals(method) || upm.method().equals("*");
            if (match && methodMatch)
                return true;
        }

        return false;
    }

    private static record UrlPathMethod(String url, String method) {
    }

}
