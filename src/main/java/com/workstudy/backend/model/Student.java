package com.workstudy.backend.model;

import jakarta.persistence.*;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    private String role;
    private String authProvider;
    private String mfaCode;
    
    private String masterResumePath;

    public String getAuthProvider() {
        return authProvider;
    }
    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getMfaCode() {
        return mfaCode;
    }
    public void setMfaCode(String mfaCode) {
        this.mfaCode = mfaCode;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getMasterResumePath() {
        return masterResumePath;
    }
    public void setMasterResumePath(String masterResumePath) {
        this.masterResumePath = masterResumePath;
    }
}
