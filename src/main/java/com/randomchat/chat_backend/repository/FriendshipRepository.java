package com.randomchat.chat_backend.repository;

import com.randomchat.chat_backend.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Single query to handle bidirectional friendship lookup
    @Query("SELECT f FROM Friendship f " +
            "WHERE (f.userId = :userId AND f.friendId = :friendId) " +
            "OR (f.userId = :friendId AND f.friendId = :userId)")
    Optional<Friendship> findByUserIds(String userId, String friendId);

    // Boolean check for friendship existence
    @Query("SELECT COUNT(f) > 0 FROM Friendship f " +
            "WHERE (f.userId = :userId AND f.friendId = :friendId) " +
            "OR (f.userId = :friendId AND f.friendId = :userId)")
    boolean existsByUserIds(String userId, String friendId);

    // Fetch list of friend IDs by user ID
    @Query("SELECT CASE " +
            "WHEN f.userId = :userId THEN f.friendId " +
            "WHEN f.friendId = :userId THEN f.userId " +
            "END " +
            "FROM Friendship f " +
            "WHERE f.userId = :userId OR f.friendId = :userId")
    List<String> findFriendIdsByUserId(String userId);
}
