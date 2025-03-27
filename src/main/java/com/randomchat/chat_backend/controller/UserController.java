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
        System.out.println("inside get usersa...............................");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        System.out.println("inside delete user..............................."+ userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // New endpoint to get profile picture URIs
    @GetMapping("/profile-pictures")
    public ResponseEntity<List<String>> getProfilePictureUris() {
        List<String> profilePictures = List.of(
                "https://i.ibb.co/XfHCgYHd/boy1.png",
                "https://i.ibb.co/TMnTkX9F/boy2.png",
                "https://i.ibb.co/7dmQznYZ/boy3.png",
                "https://i.ibb.co/B5P4HQ7m/boy4.png",
                "https://i.ibb.co/B2jmTcVm/boy5.png",
                "https://i.ibb.co/SXv9zqq6/boy6.png",
                "https://i.ibb.co/Z6kVwmgm/boy7.png",
                "https://i.ibb.co/dwgpHgYP/boy8.png",
                "https://i.ibb.co/sdQtCM42/girl1.png",
                "https://i.ibb.co/JjQZXnWK/girl2.png",
                "https://i.ibb.co/27MrMqF4/girl3.png",
                "https://i.ibb.co/mF6rqKnH/girl4.png",
                "https://i.ibb.co/vCJ04pGT/girl5.png",
                "https://i.ibb.co/7xZ0LtFD/girl6.png",
                "https://i.ibb.co/rRVH2XZq/Girl7.png",
                "https://i.ibb.co/Lzrt8VXF/girl8.png"
        );
        return ResponseEntity.ok(profilePictures);
    }
}
