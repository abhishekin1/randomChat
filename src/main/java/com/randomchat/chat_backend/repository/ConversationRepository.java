package com.randomchat.chat_backend.repository;

import com.randomchat.chat_backend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId")
    List<Conversation> findByUserId(@Param("userId") String userId);

    @Query("SELECT c FROM Conversation c WHERE (c.user1Id = :user1Id AND c.user2Id = :user2Id) OR (c.user1Id = :user2Id AND c.user2Id = :user1Id)")
    Optional<Conversation> findBetweenUsers(@Param("user1Id") String user1Id, @Param("user2Id") String user2Id);

}
