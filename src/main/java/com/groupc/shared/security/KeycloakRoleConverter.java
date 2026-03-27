package com.groupc.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@code KeycloakRoleConverter} är en Spring {@link Converter} som ansvarar för att extrahera
 * och omvandla roller från ett Keycloak JWT (JSON Web Token) till Spring Security
 * {@link GrantedAuthority}-objekt. Denna konverterare är avgörande för att integrera
 * Keycloak-baserad rollhantering med Spring Securitys auktoriseringsmekanismer.
 *
 * <p>Konverteraren utökar standardfunktionaliteten från {@link JwtGrantedAuthoritiesConverter}
 * genom att specifikt läsa roller från Keycloaks {@code realm_access}-claim i JWT:n.
 * Dessa roller prefixas med "ROLE_" och konverteras till versaler för att följa
 * Spring Securitys konvention för rollnamn (t.ex. "admin" blir "ROLE_ADMIN").
 *
 * <p>Denna klass används typiskt i konfigurationen av Spring Security för att anpassa
 * hur JWT:er bearbetas och hur användarens behörigheter fastställs baserat på
 * informationen i JWT:n.
 *
 * <p><b>Funktionalitet:</b>
 * <ul>
 *     <li>Konverterar ett {@link Jwt}-objekt till ett {@link AbstractAuthenticationToken},
 *         specifikt en {@link JwtAuthenticationToken}.</li>
 *     <li>Extraherar standardbehörigheter med hjälp av {@link JwtGrantedAuthoritiesConverter}.</li>
 *     <li>Extraherar Keycloak-specifika roller från {@code realm_access.roles}-claim i JWT:n.</li>
 *     <li>Kombinerar standardbehörigheter med de extraherade Keycloak-rollerna till en
 *         samlad uppsättning av {@link GrantedAuthority}-objekt.</li>
 *     <li>Prefixar Keycloak-rollerna med "ROLE_" och konverterar dem till versaler.</li>
 * </ul>
 */
public class KeycloakRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {

        Collection<GrantedAuthority> authorities = defaultGrantedAuthoritiesConverter.convert(source);

        Collection<GrantedAuthority> keycloakAuthorities = extractResourceRoles(source);
        Collection<GrantedAuthority> allAuthorities = Stream.concat(authorities.stream(), keycloakAuthorities.stream())
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken(source, allAuthorities);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptySet();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}
