package com.tutoring.client.api;

import com.tutoring.client.model.UserDTO;

public class Session {
    private static Session instance;
    private UserDTO currentUser;
    private ApiClient apiClient;
    
    private Session() {
        this.apiClient = new ApiClient();
    }
    
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    public void login(String username, String password) {
        apiClient.setCredentials(username, password);
    }
    
    public void setCurrentUser(UserDTO user) {
        this.currentUser = user;
    }
    
    public UserDTO getCurrentUser() {
        return currentUser;
    }
    
    public ApiClient getApiClient() {
        return apiClient;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
