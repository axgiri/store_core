package github.oldLab.oldLab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.entity.Shop;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShop(Shop shop);
    
    List<Product> findByCategory(CategoryEnum category);
}
