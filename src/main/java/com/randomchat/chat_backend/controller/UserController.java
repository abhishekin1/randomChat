package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        System.out.println("inside get user..............................."+ user);
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable String userId) {
        System.out.println("inside get user..............................."+ userId);
        return ResponseEntity.ok(userService.getUserByDeviceId(userId));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        System.out.println("inside get users...............................");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        System.out.println("inside delete user..............................."+ userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
