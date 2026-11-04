package com.fundspark.fundspark_backend.controller;

import com.fundspark.fundspark_backend.model.User;
import com.fundspark.fundspark_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Strips passwordHash out before sending a user back to the frontend —
    // never return password data in an API response, even hashed.
    private Map<String, Object> sanitize(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", u.getUserId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("role", u.getRole());
        m.put("location", u.getLocation());
        m.put("bio", u.getBio());
        m.put("avatarInitials", u.getAvatarInitials());
        m.put("status", u.getStatus());
        return m;
    }

        // Mobile number must be exactly 10 digits, numbers only
    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^[0-9]{10}$");

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    private static final String PHONE_ERROR = "Mobile number must be exactly 10 digits (numbers only)";

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(sanitize(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/users?role=founder   -> powers Admin > Users page filter
    @GetMapping
    public List<Map<String, Object>> getUsers(@RequestParam(required = false) User.Role role) {
        List<User> users = (role != null) ? userRepository.findByRole(role) : userRepository.findAll();
        return users.stream().map(this::sanitize).collect(Collectors.toList());
    }

    // POST /api/users/register   -> powers Register page
    // NOTE: password is stored as plain text here for mini-project simplicity.
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getEmail() == null || user.getPasswordHash() == null || user.getFullName() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Full name, email, and password are required"));
        }
        if (!isValidPhone(user.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("message", PHONE_ERROR));
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "An account with this email already exists"));
        }
        if (user.getAvatarInitials() == null || user.getAvatarInitials().isBlank()) {
            String[] parts = user.getFullName().trim().split("\\s+");
            String initials = parts[0].substring(0, 1).toUpperCase();
            if (parts.length > 1) initials += parts[parts.length - 1].substring(0, 1).toUpperCase();
            user.setAvatarInitials(initials);
        }
        user.setStatus(User.Status.active);
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(saved));
    }

    // POST /api/users/login   -> powers Login page
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }

        return userRepository.findByEmail(email)
                .filter(u -> u.getPasswordHash().equals(password))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(sanitize(u)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password")));
    }

    // PUT /api/users/{id}   -> powers Profile pages (Founder/Investor/Admin) "Save Changes"
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Integer id, @RequestBody User updates) {
            if (updates.getPhone() != null && !updates.getPhone().isEmpty() && !isValidPhone(updates.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("message", PHONE_ERROR));
        }
        return userRepository.findById(id).map(u -> {
            if (updates.getFullName() != null) u.setFullName(updates.getFullName());
            if (updates.getPhone() != null) u.setPhone(updates.getPhone());
            if (updates.getLocation() != null) u.setLocation(updates.getLocation());
            if (updates.getBio() != null) u.setBio(updates.getBio());
            User saved = userRepository.save(u);
            return ResponseEntity.ok(sanitize(saved));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found")));
    }

    // PUT /api/users/{id}/password   -> powers Settings > Change Password
    // Requires the correct current password before allowing the change — same validation
    // approach as login, just re-checked here for security.
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        return userRepository.findById(id).<ResponseEntity<?>>map(u -> {
            if (!u.getPasswordHash().equals(currentPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Current password is incorrect"));
            }
            if (newPassword == null || newPassword.length() < 4) {
                return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 4 characters"));
            }
            u.setPasswordHash(newPassword);
            userRepository.save(u);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found")));
    }
        // DELETE /api/users/{id}   -> powers Settings > Danger Zone > Delete Account
    @DeleteMapping("/{id}")
@Transactional
public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
    if (!userRepository.existsById(id)) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
    }
    // Remove the user's investment requests first, so the delete works even if the
    // investment_requests table has no ON DELETE CASCADE foreign key.
    jdbcTemplate.update("DELETE FROM investment_requests WHERE investor_id = ?", id);
    // An account can only be deleted after an admin approved the deletion request.
Integer approved = jdbcTemplate.queryForObject(
    "SELECT COUNT(*) FROM account_deletion_requests WHERE user_id = ? AND status = 'approved'",
    Integer.class, id);
if (approved == null || approved == 0) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("message", "Admin approval is required before this account can be deleted."));
}
    userRepository.deleteById(id);
    return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
}
}
