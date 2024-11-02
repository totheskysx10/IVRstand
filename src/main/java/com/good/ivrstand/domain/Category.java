package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Сущность категории услуг.
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
     * Список дочерних категорий данной категории.
     */
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Getter
    private List<Category> childrenCategories;

    /**
     * Родительская категория данной категории.
     */
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    @Getter
    @Setter
    private Category parentCategory;

    /**
     * Ссылка на превью GIF анимации категории.
     */
    @Column(name = "cat_gif_preview")
    @Getter
    @Setter
    private String gifPreview;

    /**
     * Ссылка на GIF анимацию, связанную с категорией.
     */
    @Column(name = "cat_gif_link")
    @Getter
    @Setter
    private String gifLink;

    /**
     * Ссылка на главную иконку, связанную с категорией.
     */
    @Column(name = "cat_main_icon_link")
    @Getter
    @Setter
    private String mainIconLink;

    /**
     * Аудио заголовка категории.
     */
    @Column(name = "cat_titleaudio")
    @Getter
    @Setter
    private String titleAudio;
}
