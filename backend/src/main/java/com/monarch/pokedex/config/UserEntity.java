package com.monarch.pokedex.config;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String username;
    @Column(nullable = false) private String password;
    protected UserEntity() {}
    public UserEntity(String username, String password) { this.username = username; this.password = password; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
