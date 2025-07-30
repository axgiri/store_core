package github.oldLab.oldLab.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>{

    Page<Review> findByShopId(Long shopId, Pageable pageable);

    Page<Review> findByPersonId(Long personId, Pageable pageable);

    boolean existsByPersonIdAndAuthorId(Long personId, Long authorId);

    boolean existsByShopIdAndAuthorId(Long shopId, Long authorId);
}
