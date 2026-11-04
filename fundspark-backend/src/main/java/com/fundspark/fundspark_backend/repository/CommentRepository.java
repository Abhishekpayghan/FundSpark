package com.fundspark.fundspark_backend.repository;

import com.fundspark.fundspark_backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Ordered newest-first — matches how the frontend renders the comment list
    List<Comment> findByStartupIdOrderByCreatedAtDesc(Integer startupId);
}
