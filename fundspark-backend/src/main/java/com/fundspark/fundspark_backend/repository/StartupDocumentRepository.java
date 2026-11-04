package com.fundspark.fundspark_backend.repository;

import com.fundspark.fundspark_backend.model.StartupDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StartupDocumentRepository extends JpaRepository<StartupDocument, Integer> {

    // Fetches all documents a founder uploaded for one specific startup —
    // powers both the founder's own view and the admin verification view.
    List<StartupDocument> findByStartupId(Integer startupId);
}