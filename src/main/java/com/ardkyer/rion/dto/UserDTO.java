package com.ardkyer.rion.dto;

public class UserDTO {
    private Long id;
    private String username;
    // 필요한 다른 필드들...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // 필요한 다른 getter와 setter 메소드들...
}