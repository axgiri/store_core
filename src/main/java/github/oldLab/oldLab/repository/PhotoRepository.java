package github.oldLab.oldLab.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Photo;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByPersonId(Long personId);
    
    Optional<Photo> findByShopId(Long shopId);
}
