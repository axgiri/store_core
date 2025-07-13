package github.oldLab.oldLab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>{
    
    List<Review> findByShopId(Long shopId);

    List<Review> findByPersonId(Long personId);
}
