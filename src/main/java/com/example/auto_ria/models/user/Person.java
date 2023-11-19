package com.example.auto_ria.models.user;

import com.example.auto_ria.enums.ERole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String avatar = null;
    private String password;

    @ElementCollection
    private List<ERole> roles = new ArrayList<>();

    private Boolean isActivated = false;

    @Column(updatable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    private LocalDateTime createdAt;

    @UpdateTimestamp()
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    private LocalDateTime updatedAt;

    @Builder
    public Person(String name, String lastName, String email, String avatar, String password, List<ERole> roles) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.avatar = avatar;
        this.password = password;
        this.roles = roles;
    }

    public Person(String email, String password, List<ERole> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public Person(int id, String name, String lastName, List<ERole> roles) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        this.roles.forEach(role -> {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
            authorities.add(authority);
        });
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
