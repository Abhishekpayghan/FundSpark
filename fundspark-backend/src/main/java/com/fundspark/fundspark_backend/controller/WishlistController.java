package com.fundspark.fundspark_backend.controller;

import com.fundspark.fundspark_backend.model.Wishlist;
import com.fundspark.fundspark_backend.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    // GET /api/wishlist/investor/{investorId}   -> powers the Wishlist page
    @GetMapping("/investor/{investorId}")
    public List<Wishlist> getByInvestor(@PathVariable Integer investorId) {
        return wishlistRepository.findByInvestorId(investorId);
    }

    // POST /api/wishlist/toggle   -> powers the heart/bookmark icon (add if absent, remove if present)
    // body: { "investorId": 4, "startupId": 1 }
    @PostMapping("/toggle")
    public ResponseEntity<?> toggle(@RequestBody Map<String, Integer> body) {
        Integer investorId = body.get("investorId");
        Integer startupId = body.get("startupId");

        Optional<Wishlist> existing = wishlistRepository.findByInvestorIdAndStartupId(investorId, startupId);

        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return ResponseEntity.ok(Map.of("bookmarked", false));
        } else {
            Wishlist w = new Wishlist();
            w.setInvestorId(investorId);
            w.setStartupId(startupId);
            wishlistRepository.save(w);
            return ResponseEntity.ok(Map.of("bookmarked", true));
        }
    }
}
