package com.example.soap_crud.service;

import com.example.adservice.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActiveDirectoryService {

    @Autowired
    private LdapTemplate ldapTemplate;

    public List<User> getAllUsers() {
        return ldapTemplate.search("", "(objectClass=user)", new UserAttributesMapper());
    }

    public User getUserDetailsByCn(String cn) {
        List<User> users = ldapTemplate.search("", "(cn=" + cn + ")", new UserAttributesMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    public String createUser(User user) {
        try {
            DirContextAdapter context = new DirContextAdapter();
            context.setAttributeValues("objectClass", new String[]{"top", "person", "organizationalPerson", "user"});
            context.setAttributeValue("cn", user.getCn());
            context.setAttributeValue("sAMAccountName", user.getSamAccountName());
            context.setAttributeValue("givenName", user.getFirstName());
            context.setAttributeValue("sn", user.getLastName());
            context.setAttributeValue("userPrincipalName", user.getUserPrincipalName());
            context.setAttributeValue("mail", user.getEmail());

            ldapTemplate.bind("cn=" + user.getCn(), context, null);
            return "User created successfully: " + user.getCn();
        } catch (Exception e) {
            return "Error creating user: " + e.getMessage();
        }
    }

    public String updateUser(User user) {
        try {
            DirContextAdapter context = new DirContextAdapter("cn=" + user.getCn());
            context.setAttributeValues("objectClass", new String[]{"top", "person", "organizationalPerson", "user"});
            context.setAttributeValue("givenName", user.getFirstName());
            context.setAttributeValue("sn", user.getLastName());
            context.setAttributeValue("userPrincipalName", user.getUserPrincipalName());
            context.setAttributeValue("mail", user.getEmail());

            ldapTemplate.modifyAttributes(context);
            return "User updated successfully: " + user.getCn();
        } catch (Exception e) {
            return "Error updating user: " + e.getMessage();
        }
    }

    public String deleteUserByCn(String cn) {
        try {
            ldapTemplate.unbind("cn=" + cn);
            return "User deleted successfully: " + cn;
        } catch (Exception e) {
            return "Error deleting user: " + e.getMessage();
        }
    }

    public String enableUserByCn(String cn) {
        try {
            DirContextOperations context = ldapTemplate.lookupContext("cn=" + cn);
            context.setAttributeValue("userAccountControl", "512"); // Normal account
            ldapTemplate.modifyAttributes(context);
            return "User enabled successfully: " + cn;
        } catch (Exception e) {
            return "Error enabling user: " + e.getMessage();
        }
    }

    public String disableUserByCn(String cn) {
        try {
            DirContextOperations context = ldapTemplate.lookupContext("cn=" + cn);
            context.setAttributeValue("userAccountControl", "514"); // Disabled account
            ldapTemplate.modifyAttributes(context);
            return "User disabled successfully: " + cn;
        } catch (Exception e) {
            return "Error disabling user: " + e.getMessage();
        }
    }

    public List<String> getAllGroups() {
        try {
            return ldapTemplate.search("", "(objectClass=group)", (AttributesMapper<String>) attrs ->
                    (String) attrs.get("cn").get());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String addUserToGroups(String cn, List<String> groups) {
        try {
            for (String group : groups) {
                ldapTemplate.modifyAttributes("cn=" + group, new ModificationItem[]{
                        new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", "cn=" + cn))
                });
            }
            return "User added to groups successfully: " + groups;
        } catch (Exception e) {
            return "Error adding user to groups: " + e.getMessage();
        }
    }

    public String removeUserFromGroups(String cn, List<String> groups) {
        try {
            for (String group : groups) {
                ldapTemplate.modifyAttributes("cn=" + group, new ModificationItem[]{
                        new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("member", "cn=" + cn))
                });
            }
            return "User removed from groups successfully: " + groups;
        } catch (Exception e) {
            return "Error removing user from groups: " + e.getMessage();
        }
    }

    // Updated UserAttributesMapper as a private static inner class
    private static class UserAttributesMapper implements AttributesMapper<User> {
        @Override
        public User mapFromAttributes(Attributes attrs) throws NamingException {
            User user = new User();

            user.setCn(getAttributeValue(attrs, "cn"));
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
