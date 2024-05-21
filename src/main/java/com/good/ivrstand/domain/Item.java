package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Класс, представляющий сущность услуги.
 */
@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {

    /**
     * Идентификатор услуги.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    @Getter
    private long id;

    /**
     * Заголовок услуги.
     */
    @Column(name = "item_title")
    @Getter
    @Setter
    @NonNull
    private String title;

    /**
     * Описание услуги.
     */
    @Column(name = "item_description")
    @Getter
    @Setter
    @NonNull
    private String description;

    /**
     * Ссылка на первью GIF анимаци услуги.
     */
    @Column(name = "gif_preview")
    @Getter
    @Setter
    private String gifPreview;

    /**
     * Ссылка на GIF анимацию, связанную с услугой.
     */
    @Column(name = "gif_link")
    @Getter
    @Setter
    private String gifLink;

    /**
     * Категория, к которой относится услуга.
     */
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Дополнения к услуге.
     */
    @Getter
    @Setter
    @OneToMany(mappedBy = "item", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<Addition> additions;
}
