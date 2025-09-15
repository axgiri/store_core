package github.oldLab.oldLab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Shop;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByCategoryIn(List<CategoryEnum> categories);

    Optional<Shop> findById(Long id);
}
