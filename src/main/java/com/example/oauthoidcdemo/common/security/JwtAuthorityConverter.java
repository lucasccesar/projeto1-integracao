package com.example.oauthoidcdemo.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

@Component
public class JwtAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String API_CLIENT_ID = "orders-api";

    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);
        LinkedHashSet<GrantedAuthority> authorities = new LinkedHashSet<>(scopeAuthorities);
        addClientRoles(jwt, authorities);
        return authorities;
    }

    private void addClientRoles(Jwt jwt, Collection<GrantedAuthority> authorities) {
        Object resourceAccessClaim = jwt.getClaims().get("resource_access");
        if (!(resourceAccessClaim instanceof Map<?, ?> resourceAccess)) {
            return;
        }

        Object apiAccessClaim = resourceAccess.get(API_CLIENT_ID);
        if (!(apiAccessClaim instanceof Map<?, ?> apiAccess)) {
            return;
        }

        Object rolesClaim = apiAccess.get("roles");
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return;
        }

        roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
                .forEach(authorities::add);
    }
}
