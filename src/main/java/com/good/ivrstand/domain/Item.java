package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

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
     * Ссылка на GIF анимацию, связанную с услугой.
     */
    @Column(name = "gif_link")
    @Getter
    @Setter
    @NonNull
    private String gifLink;

    /**
     * Категория, к которой относится услуга.
     */
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
