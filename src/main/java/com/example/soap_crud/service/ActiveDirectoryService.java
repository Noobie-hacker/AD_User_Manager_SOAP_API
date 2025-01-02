package com.example.soap_crud.service;

import com.example.adservice.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
public class ActiveDirectoryService {

    @Autowired
    private LdapTemplate ldapTemplate;

    public List<User> getAllUsers() {
        return ldapTemplate.search("", "(objectClass=user)", new UserAttributesMapper());
    }

    public User getUserDetails(String samAccountName) {
        List<User> users = ldapTemplate.search("", "(samAccountName=" + samAccountName + ")", new UserAttributesMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    // Updated UserAttributesMapper as a private static inner class
    private static class UserAttributesMapper implements AttributesMapper<User> {
        @Override
        public User mapFromAttributes(Attributes attrs) throws NamingException {
            User user = new User();

            user.setSamAccountName(getAttributeValue(attrs, "sAMAccountName"));
            user.setDistinguishedName(getAttributeValue(attrs, "distinguishedName"));
            user.setUserPrincipalName(getAttributeValue(attrs, "userPrincipalName"));
            user.setObjectGUID(getAttributeValue(attrs, "objectGUID"));
            user.setFirstName(getAttributeValue(attrs, "givenName"));
            user.setLastName(getAttributeValue(attrs, "sn"));
            user.setEmail(getAttributeValue(attrs, "mail"));

            if (attrs.get("memberOf") != null) {
                for (int i = 0; i < attrs.get("memberOf").size(); i++) {
                    user.getMemberOf().add(attrs.get("memberOf").get(i).toString());
                }
            }

            return user;
        }

        private String getAttributeValue(Attributes attrs, String attributeName) throws NamingException {
            return attrs.get(attributeName) != null ? (String) attrs.get(attributeName).get() : "";
        }
    }
}
