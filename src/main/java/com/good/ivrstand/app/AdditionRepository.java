package com.good.ivrstand.app;

import com.good.ivrstand.domain.Addition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionRepository extends JpaRepository<Addition, Long> {

    Addition findById(long id);

    void deleteById(long id);

    Page<Addition> findByItemId(long itemId, Pageable pageable);
}
