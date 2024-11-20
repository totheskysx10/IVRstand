package com.good.ivrstand.app.repository;

import com.good.ivrstand.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findById(long id);
    void deleteById(long id);
    Page<Item> findByCategoryId(long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.category IS NULL")
    Page<Item> findItemsWithNullCategory(Pageable pageable);

    @Query("SELECT i from Item i WHERE i.descriptionHash = :descriptionHash AND SIZE(i.audio) > 0")
    Page<Item> findByHashAndAudioExistence(@Param("descriptionHash") String descriptionHash, Pageable pageable);
}