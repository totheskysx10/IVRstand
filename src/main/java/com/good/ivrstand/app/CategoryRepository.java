package com.good.ivrstand.app;

import com.good.ivrstand.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findById(long id);
    void deleteById(long id);
    Page<Category> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE SIZE(c.childrenCategories) = 0 AND c.parentCategory IS NULL")
    Page<Category> findUnallocatedCategories(Pageable pageable);
}
