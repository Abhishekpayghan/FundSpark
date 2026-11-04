package com.fundspark.fundspark_backend.controller;

import com.fundspark.fundspark_backend.model.StartupDocument;
import com.fundspark.fundspark_backend.repository.StartupDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Lets a founder attach any number of genuine business documents to a startup
// (GST certificate, Shop Act license, PAN, or anything else they actually have),
// and lets an admin retrieve that list to verify the campaign before approving it.
@RestController
@RequestMapping("/api/startup-documents")
@CrossOrigin(origins = "*")
public class StartupDocumentController {

    @Autowired
    private StartupDocumentRepository startupDocumentRepository;

    // GET /api/startup-documents/startup/{startupId}
    // Powers both "My Startups" (founder sees what they uploaded) and
    // Admin > Campaign Approval (admin sees what to verify).
    @GetMapping("/startup/{startupId}")
    public List<StartupDocument> getDocumentsForStartup(@PathVariable Integer startupId) {
        return startupDocumentRepository.findByStartupId(startupId);
    }

    // POST /api/startup-documents
    // Body: { "startupId": 5, "documentName": "GST Certificate", "documentUrl": "http://localhost:8080/uploads/xxx.pdf" }
    @PostMapping
    public ResponseEntity<?> addDocument(@RequestBody StartupDocument document) {
        if (document.getStartupId() == null || document.getDocumentName() == null || document.getDocumentUrl() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "startupId, documentName and documentUrl are required"));
        }
        StartupDocument saved = startupDocumentRepository.save(document);
        return ResponseEntity.ok(saved);
    }

    // DELETE /api/startup-documents/{id} — lets a founder remove a wrongly uploaded document
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Integer id) {
        if (!startupDocumentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        startupDocumentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Document removed"));
    }
}
