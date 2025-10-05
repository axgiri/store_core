package github.oldLab.oldLab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByPerson(Person person);
    Page<Product> findByPersonId(Long personId, Pageable pageable);

    Page<Product> findByCategory(CategoryEnum category, Pageable pageable);
}
