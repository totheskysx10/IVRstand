package com.good.ivrstand.app;

import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findById(long id);
    void deleteById(long id);
    Page<Category> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
