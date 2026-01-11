package com.randomchat.chat_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class User implements UserDetails{

    @Id
    private String username;

    private String password;

    private String name;

    private String email;

    private String gender;

    private String location;
    private boolean isOnline;
    private Integer suspectLevel = 100;

    private String bio;
    private String photoUrl;
    private LocalDateTime lastOnline;
    private String fcmToken;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

}
