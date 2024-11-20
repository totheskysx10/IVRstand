package com.good.ivrstand.app.repository;

import com.good.ivrstand.domain.Addition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionRepository extends JpaRepository<Addition, Long> {
    Addition findById(long id);
    void deleteById(long id);
    Page<Addition> findByItemId(long itemId, Pageable pageable);

    @Query("SELECT a from Addition a WHERE a.descriptionHash = :descriptionHash AND SIZE(a.audio) > 0")
    Page<Addition> findByHashAndAudioExistence(@Param("descriptionHash") String descriptionHash, Pageable pageable);
}
