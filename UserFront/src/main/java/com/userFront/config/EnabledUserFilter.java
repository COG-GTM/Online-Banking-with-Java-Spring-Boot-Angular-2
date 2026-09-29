package com.userFront.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import com.userFront.dao.UserDao;
import com.userFront.domain.User;

/**
 * Re-checks on every request that the authenticated user still exists and is enabled,
 * logging out sessions whose account has been disabled.
 */
public class EnabledUserFilter extends OncePerRequestFilter {

    public static final String DISABLED_URL = "/index?disabled";

    private final UserDao userDao;

    private final SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

    private final CookieClearingLogoutHandler rememberMeCookieClearer = new CookieClearingLogoutHandler("remember-me");

    public EnabledUserFilter(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            User current = userDao.findByUsername(username);
            if (current == null || !current.isEnabled()) {
                rememberMeCookieClearer.logout(request, response, authentication);
                securityContextLogoutHandler.logout(request, response, authentication);
                response.sendRedirect(request.getContextPath() + DISABLED_URL);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
