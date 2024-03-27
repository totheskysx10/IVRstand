package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Класс, представляющий сущность категории услуг.
 */
@Entity
@Table(name = "categories")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Category {
    /**
     * Идентификатор категории.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @Getter
    @EqualsAndHashCode.Include
    private long id;

    /**
     * Название категории.
     */
    @Getter
    @Setter
    @Column(name = "category_title")
    @NonNull
    @EqualsAndHashCode.Include
    private String title;

    /**
     * Список услуг, относящихся к данной категории.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Getter
    private List<Item> itemsInCategory;

    /**
     * Устанавливает список услуг, относящихся к данной категории.
     *
     * @param items Список услуг.
     */
    public void setItemsInCategoryIfEmpty(List<Item> items) {
        if (items.isEmpty())
            this.itemsInCategory = items;
    }
}
