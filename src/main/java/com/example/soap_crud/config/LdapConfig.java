package com.example.soap_crud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LdapConfig {

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://192.168.5.132:389");
        contextSource.setBase("dc=mylab,dc=local");
        contextSource.setUserDn("cn=Administrator,cn=Users,dc=mylab,dc=local");
        contextSource.setPassword("Bappy@2580");

        // Set referral policy to ignore
        Map<String, Object> envProperties = new HashMap<>();
        envProperties.put("java.naming.referral", "ignore");
        contextSource.setBaseEnvironmentProperties(envProperties);

        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }
}