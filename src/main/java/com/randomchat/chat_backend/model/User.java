package com.randomchat.chat_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    private String deviceId;

    private String name;
    private String gender;
    private String location;
    private String status;

    private Integer suspectLevel = 100;

    private String bio;
    private String photoId;
    private LocalDateTime lastOnline;
    private String fcmToken;

}
