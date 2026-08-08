package com.fundspark.fundspark_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fundspark.fundspark_backend.model.Startup;
import com.fundspark.fundspark_backend.repository.StartupRepository;

@RestController
@RequestMapping("/api/startups")
@CrossOrigin(origins = "*")   // allows your frontend (running on Live Server / a different port) to call this API.
                              // Tighten this to your actual frontend URL before final submission if your professor cares about that.
public class StartupController {

    @Autowired
    private StartupRepository startupRepository;

    // GET /api/startups            -> powers the Explore page grid
    @GetMapping
    public List<Startup> getAllStartups() {
        return startupRepository.findAll();
    }

    // GET /api/startups/{id}       -> powers the Startup Details page
    @GetMapping("/{id}")
    public ResponseEntity<Startup> getStartupById(@PathVariable Integer id) {
        return startupRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/startups/category/{categoryId}   -> powers category filter on Explore
    @GetMapping("/category/{categoryId}")
    public List<Startup> getByCategory(@PathVariable Integer categoryId) {
        return startupRepository.findByCategoryId(categoryId);
    }

    // GET /api/startups/verified    -> only verified campaigns
    @GetMapping("/verified")
    public List<Startup> getVerifiedStartups() {
        return startupRepository.findByIsVerifiedTrue();
    }

    // POST /api/startups            -> powers Founder "Create Startup" form
    @PostMapping
    public Startup createStartup(@RequestBody Startup startup) {
        startup.setStatus(Startup.Status.pending);  // every new campaign starts pending admin approval
        return startupRepository.save(startup);
    }

    // PUT /api/startups/{id}/verify -> powers Admin "Approve" button
    @PutMapping("/{id}/verify")
    public ResponseEntity<Startup> verifyStartup(@PathVariable Integer id) {
        return startupRepository.findById(id).map(startup -> {
            startup.setIsVerified(true);
            startup.setStatus(Startup.Status.live);
            return ResponseEntity.ok(startupRepository.save(startup));
        }).orElse(ResponseEntity.notFound().build());
    }
}
