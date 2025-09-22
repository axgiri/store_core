package github.oldLab.oldLab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Photo;
import github.oldLab.oldLab.service.ProductExistCountProjection;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByPersonId(Long personId);
    
    Optional<Photo> findByShopId(Long shopId);

    @Query("select count(p) from Photo p where p.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM products pr WHERE pr.id = :productId) AS exists, "
        + "(SELECT COUNT(*) FROM photos ph WHERE ph.product_id = :productId) AS count",
        nativeQuery = true)
    ProductExistCountProjection findProductExistsAndPhotoCount(@Param("productId") Long productId);


    List<Photo> findAllByProductId(Long productId);

    Optional<Photo> findByProductIdAndObjectKey(Long productId, String objectKey);

    Optional<Photo> findByProductIdAndId(Long productId, Long Id);
}
