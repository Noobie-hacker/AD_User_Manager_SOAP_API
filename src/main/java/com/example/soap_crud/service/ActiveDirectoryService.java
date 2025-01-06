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
        return setUserAccountControl(cn, 512); // Enable user account
    }

    public String disableUserByCn(String cn) {
        try {
            // Lookup the context of the user in AD
            DirContextOperations context = ldapTemplate.lookupContext("cn=" + cn);

            // Set the 'userAccountControl' attribute to disable the user
            context.setAttributeValue("userAccountControl", "514"); // 514 = Disabled account
            ldapTemplate.modifyAttributes(context);

            // Return success message
            return "User disabled successfully: " + cn;
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error disabling user: " + e.getMessage());

            // Return failure message
            return "Failed to disable user: " + e.getMessage();
        }
    }


    private String setUserAccountControl(String cn, int controlValue) {
        try {
            DirContextOperations context = ldapTemplate.lookupContext("cn=" + cn);
            context.setAttributeValue("userAccountControl", String.valueOf(controlValue));
            ldapTemplate.modifyAttributes(context);
            return "User account control updated successfully for: " + cn;
        } catch (Exception e) {
            return "Error updating user account control: " + e.getMessage();
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

    private static class UserAttributesMapper implements AttributesMapper<User> {
        @Override
        public User mapFromAttributes(Attributes attrs) throws NamingException {
            User user = new User();

            user.setCn(getAttributeValue(attrs, "cn"));
            user.setSamAccountName(getAttributeValue(attrs, "sAMAccountName"));
            user.setDistinguishedName(getAttributeValue(attrs, "distinguishedName"));
            user.setUserPrincipalName(getAttributeValue(attrs, "userPrincipalName"));

            // Convert objectGUID to UUID
            byte[] objectGUIDBytes = attrs.get("objectGUID") != null ? (byte[]) attrs.get("objectGUID").get() : null;
            user.setObjectGUID(convertObjectGUIDToUUID(objectGUIDBytes));

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

        private String convertObjectGUIDToUUID(byte[] objectGUID) {
            if (objectGUID == null || objectGUID.length != 16) {
                return null;
            }

            return String.format("%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                    objectGUID[3], objectGUID[2], objectGUID[1], objectGUID[0],
                    objectGUID[5], objectGUID[4],
                    objectGUID[7], objectGUID[6],
                    objectGUID[8], objectGUID[9],
                    objectGUID[10], objectGUID[11], objectGUID[12], objectGUID[13], objectGUID[14], objectGUID[15]);
        }
    }
}
