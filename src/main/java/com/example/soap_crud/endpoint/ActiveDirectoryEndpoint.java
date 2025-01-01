package com.example.soap_crud.endpoint;

import com.example.adservice.*;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ActiveDirectoryEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/adservice";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createUserRequest")
    @ResponsePayload
    public CreateUserResponse createUser(@RequestPayload CreateUserRequest request) {
        CreateUserResponse response = new CreateUserResponse();
        response.setStatus("SUCCESS");
        response.setMessage("User " + request.getUsername() + " created successfully.");
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserRequest")
    @ResponsePayload
    public GetUserResponse getUser(@RequestPayload GetUserRequest request) {
        GetUserResponse response = new GetUserResponse();
        response.setUsername(request.getUsername());
        response.setEmail("example@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        return response;
    }

    // Implement updateUser and deleteUser similarly
}

