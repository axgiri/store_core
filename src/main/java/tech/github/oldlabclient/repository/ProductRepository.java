package tech.github.oldlabclient.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import tech.github.oldlabclient.Enum.CategoryEnum;
import tech.github.oldlabclient.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByPersonId(UUID personId, Pageable pageable);

    Page<Product> findByCategory(CategoryEnum category, Pageable pageable);
}
