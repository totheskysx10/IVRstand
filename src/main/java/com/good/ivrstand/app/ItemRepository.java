package com.good.ivrstand.app;

import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findById(long id);
    void deleteById(long id);
    Page<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Item> findByCategoryIdAndTitleContainingIgnoreCase(long categoryId, String title, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.category IS NULL")
    Page<Item> findItemsWithNullCategory(Pageable pageable);
}
