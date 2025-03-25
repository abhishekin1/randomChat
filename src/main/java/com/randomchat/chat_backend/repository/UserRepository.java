package com.randomchat.chat_backend.repository;

import com.randomchat.chat_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // ✅ Find users by status
    List<User> findByStatus(String status);

    // ✅ Find users by location
    List<User> findByLocation(String location);

    // ✅ Find users by gender
    List<User> findByGender(String gender);

    // ✅ Find users by suspect level
    List<User> findBySuspectLevel(User.SuspectLevel suspectLevel);

    // ✅ Find users by last online timestamp (for filtering active users)
    List<User> findByLastOnlineAfter(java.time.LocalDateTime timestamp);

    // Replace findByIdIn with:
    List<User> findByDeviceIdIn(List<String> deviceIds);

}
