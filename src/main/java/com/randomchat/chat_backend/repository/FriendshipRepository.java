package com.randomchat.chat_backend.repository;

import com.randomchat.chat_backend.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByFriendIdAndStatus(String friendId, Friendship.FriendshipStatus status);
    List<Friendship> findByUserIdAndStatus(String userId, Friendship.FriendshipStatus status);
    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' AND (f.userId = :userId OR f.friendId = :userId)")
    List<Friendship> findAllAcceptedFriendships(String userId);

    @Modifying
    @Query("DELETE FROM Friendship f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    void deleteByUserIds(String userId, String friendId);

    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    Optional<Friendship> findBetweenUsers(String userId, String friendId);





}
