package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Create or update a user
    public User saveUser(User user) {
        user.setLastOnline(LocalDateTime.now()); // Set last online time
        return userRepository.save(user);
    }

    // ✅ Find user by device ID
    public Optional<User> getUserByDeviceId(String deviceId) {
        return userRepository.findById(deviceId);
    }

    // ✅ Find users by status
    public List<User> getUsersByStatus(String status) {
        return userRepository.findByStatus(status);
    }

    // ✅ Find recently active users (e.g., in the last 10 minutes)
    public List<User> getRecentlyActiveUsers() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        return userRepository.findByLastOnlineAfter(tenMinutesAgo);
    }

    // ✅ Delete user by ID
    public void deleteUser(String deviceId) {
        userRepository.deleteById(deviceId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByIds(List<String> friendIds) {
        return userRepository.findByUsernameIn(friendIds);
    }
}
