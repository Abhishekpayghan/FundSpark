package com.fundspark.fundspark_backend.controller;

import com.fundspark.fundspark_backend.model.Comment;
import com.fundspark.fundspark_backend.model.Notification;
import com.fundspark.fundspark_backend.model.Startup;
import com.fundspark.fundspark_backend.model.User;
import com.fundspark.fundspark_backend.repository.CommentRepository;
import com.fundspark.fundspark_backend.repository.NotificationRepository;
import com.fundspark.fundspark_backend.repository.StartupRepository;
import com.fundspark.fundspark_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired private CommentRepository commentRepository;
    @Autowired private StartupRepository startupRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    // GET /api/comments/startup/{startupId}   -> powers the Comments tab list
    @GetMapping("/startup/{startupId}")
    public List<Map<String, Object>> getByStartup(@PathVariable Integer startupId) {
        String sql =
            "SELECT c.comment_id AS id, c.comment_text AS text, c.likes, c.created_at AS createdAt, " +
            "u.user_id AS userId, u.full_name AS name, u.avatar_initials AS initials " +
            "FROM comments c JOIN users u ON u.user_id = c.user_id " +
            "WHERE c.startup_id = ? ORDER BY c.created_at DESC";
        return jdbcTemplate.queryForList(sql, startupId);
    }

    // GET /api/comments/user/{userId}
    // Powers the Investor Dashboard "Activity Timeline" — comments THIS user has posted,
    // with the startup name they commented on.
    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getByUser(@PathVariable Integer userId) {
        String sql =
            "SELECT c.comment_id AS id, c.comment_text AS text, c.created_at AS createdAt, " +
            "s.startup_id AS startupId, s.name AS startupName " +
            "FROM comments c JOIN startups s ON s.startup_id = c.startup_id " +
            "WHERE c.user_id = ? ORDER BY c.created_at DESC";
        return jdbcTemplate.queryForList(sql, userId);
    }

    // GET /api/comments/founder/{founderId}/feed
    // Powers the Founder Dashboard "Activity Timeline" — comments received on ANY of this
    // founder's startups, with the real commenter name and which startup it was on.
    @GetMapping("/founder/{founderId}/feed")
    public List<Map<String, Object>> getByFounderFeed(@PathVariable Integer founderId) {
        String sql =
            "SELECT c.comment_id AS id, c.comment_text AS text, c.created_at AS createdAt, " +
            "u.full_name AS commenterName, s.name AS startupName " +
            "FROM comments c " +
            "JOIN users u ON u.user_id = c.user_id " +
            "JOIN startups s ON s.startup_id = c.startup_id " +
            "WHERE s.founder_id = ? ORDER BY c.created_at DESC LIMIT 10";
        return jdbcTemplate.queryForList(sql, founderId);
    }

    // POST /api/comments   -> powers the "Post Comment" form
    // Also auto-notifies the founder of the commented-on startup.
    @Transactional
    @PostMapping
    public Comment addComment(@RequestBody Comment comment) {
        comment.setLikes(0);
        Comment saved = commentRepository.save(comment);

        Startup startup = startupRepository.findById(comment.getStartupId()).orElse(null);
        User commenter = userRepository.findById(comment.getUserId()).orElse(null);
        if (startup != null && commenter != null) {
            Notification n = new Notification();
            n.setUserId(startup.getFounderId());
            n.setIcon("message-circle");
            n.setTitle("New comment");
            n.setMessage(commenter.getFullName() + " commented on " + startup.getName() + ".");
            n.setIsRead(false);
            notificationRepository.save(n);
        }

        return saved;
    }

    // PUT /api/comments/{id}/like   -> powers the thumbs-up icon
    @PutMapping("/{id}/like")
    public ResponseEntity<Comment> likeComment(@PathVariable Integer id) {
        return commentRepository.findById(id).map(comment -> {
            comment.setLikes(comment.getLikes() + 1);
            return ResponseEntity.ok(commentRepository.save(comment));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/comments/{id}   -> optional moderation / self-delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer id) {
        if (!commentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        commentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
