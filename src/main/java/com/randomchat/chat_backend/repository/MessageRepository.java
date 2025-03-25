package com.randomchat.chat_backend.repository;

import com.randomchat.chat_backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ✅ Find all messages in a conversation (sorted by timestamp)
    List<Message> findByConversationIdOrderByTimeStampAsc(Long conversationId);

    // ✅ Find all messages sent by a specific user
    List<Message> findBySenderId(String senderId);

    // ✅ Find all messages received by a specific user
    List<Message> findByReceiverId(String receiverId);

    // ✅ Find messages by status (for filtering unread, delivered, etc.)
    List<Message> findByStatus(String status);

    // ✅ Find by referred message (for replies or references)
    List<Message> findByReferredMessageId(Long referredMessageId);

    // ✅ Find messages between two users (both directions)
    @Query("SELECT m FROM Message m " +
            "WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) " +
            "OR (m.senderId = :userId2 AND m.receiverId = :userId1) " +
            "ORDER BY m.timeStamp ASC")
    List<Message> findMessagesBetweenUsers(String userId1, String userId2);

    // ✅ Find the most recent message in a conversation
    Optional<Message> findTopByConversationIdOrderByTimeStampDesc(Long conversationId);
}
