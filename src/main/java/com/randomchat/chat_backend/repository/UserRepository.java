package com.randomchat.chat_backend.repository;

import com.randomchat.chat_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    // Replace findByIdIn with:
    List<User> findByUsernameIn(List<String> usernames);

}
