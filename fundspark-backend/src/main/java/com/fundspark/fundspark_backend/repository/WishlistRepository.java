package com.fundspark.fundspark_backend.repository;

import com.fundspark.fundspark_backend.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    List<Wishlist> findByInvestorId(Integer investorId);

    Optional<Wishlist> findByInvestorIdAndStartupId(Integer investorId, Integer startupId);
}
