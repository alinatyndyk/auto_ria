package com.example.auto_ria.models;

import com.example.auto_ria.enums.ERole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
//    @JsonView(ViewsUser.SL1.class)  //todo json view
    private int id;

    @NotBlank(message = "name is required")
    @Size(min = 3, message = "name must have more than 3 characters")
    @Size(max = 255, message = "name must have less than 255 characters")
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String name;

    @Column(unique = true)
    @Size(min = 3, message = "email must have more than 3 characters")
    @Size(max = 255, message = "email must have less than 255 characters")
    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email")
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String email;

    private String avatar = null;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Invalid password. Must contain: uppercase letter, lowercase letter, number, special character. " +
                    "At least 8 characters long")
    private String password;

    @ElementCollection
    private List<ERole> roles = new ArrayList<>();

    private String refreshToken; // todo schedule cron isAccountNonLocked;

    private Boolean isActivated = false;

    @Column(updatable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    private LocalDateTime createdAt;

    @UpdateTimestamp()
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    private LocalDateTime updatedAt;

    @Builder
    public Person(String name, String email, String avatar, String password, List<ERole> roles) {
        this.name = name;
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
