package com.example.auto_ria.models;

import com.example.auto_ria.enums.EAccountType;
import com.example.auto_ria.enums.ERegion;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.enums.ESeller;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
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
@ToString
public class Seller implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonView(ViewsUser.SL1.class)
    private int id;

    @NotBlank(message = "name is required")
    @Size(min = 3, message = "name must have more than 3 characters")
    @Size(max = 255, message = "name must have less than 255 characters")
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String name;

    private String city; //todo search

    @Enumerated(EnumType.STRING)
    private ERegion region;

    @Enumerated(EnumType.STRING)
    private ERole role;

    @Enumerated(EnumType.STRING)
    private ESeller sellerType;

    @Column(unique = true) // todo add regex expressions
    @Size(min = 3, message = "email must have more than 3 characters")
    @Size(max = 255, message = "email must have less than 255 characters")
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String email;

    @Column(unique = true) // todo add regex expressions
//    @JsonView({ViewsUser.SL1.class, ViewsUser.NoSL.class})
    private String number;

    private String avatar = null;

    private String password;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "seller_cars",
            joinColumns = @JoinColumn(name = "seller_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private List<Car> cars = new ArrayList<>();

    private Boolean isActivated = false;

    private String refreshToken;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private EAccountType accountType = EAccountType.BASIC;

    public Seller(String name, String city, ERegion region, ERole role, String email, String number, String avatar, String password, ESeller sellerType) {
        this.name = name;
        this.city = city;
        this.region = region;
        this.role = role;
        this.email = email;
        this.number = number;
        this.avatar = avatar;
        this.password = password;
        this.sellerType = sellerType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()), new SimpleGrantedAuthority(sellerType.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
