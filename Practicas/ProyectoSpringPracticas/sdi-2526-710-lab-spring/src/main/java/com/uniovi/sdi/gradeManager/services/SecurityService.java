package com.uniovi.sdi.gradeManager.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
@Service
public class SecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    private final AuthenticationManager authenticationManager;

    public SecurityService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public String findLoggedInDni() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails user) {
            return user.getUsername();
        }
        if (principal instanceof String string) { // "anonymousUser" a veces
            return string;
        }
        return null;
    }

    public void autoLogin(String dni, String password) {
        UsernamePasswordAuthenticationToken authRequest = new
                UsernamePasswordAuthenticationToken(dni, password);
        Authentication authResult = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        logger.debug("Auto login {} successfully!", dni);
    }
}