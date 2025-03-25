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
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;      // ID of the initiator
    private Long friendId;    // ID of the friend

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;    // PENDING, ACCEPTED, BLOCKED

    // Embedded Enum
    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        BLOCKED
    }
}
