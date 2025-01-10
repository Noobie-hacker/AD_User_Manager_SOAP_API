package com.example.soap_crud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    private static final String DOMAIN = "mylab.local";
    private static final String URL = "ldaps://WIN-KURQEVBDCT8.mylab.local:636/";
    private static final String GROUP_SEARCH_FILTER =
            "(&(objectClass=user)(memberOf=CN=API_User,CN=Users,DC=mylab,DC=local))";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Allow unauthenticated access for SOAP service during debugging
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()  // Adjust later for authenticated access
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())  // Enable basic authentication
                .csrf(csrf -> csrf.disable())         // Disable CSRF for API-based services
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // Return 401 Unauthorized
                );
        return http.build();
    }

    @Bean
    public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider =
                new ActiveDirectoryLdapAuthenticationProvider(DOMAIN, URL);

        // Set search filter to restrict group access
        provider.setSearchFilter(GROUP_SEARCH_FILTER);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
