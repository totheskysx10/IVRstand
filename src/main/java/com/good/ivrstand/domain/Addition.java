package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Класс, представляющий сущность дополнения к услуге.
 */
@Entity
@Table(name = "additions")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Addition {

    /**
     * Идентификатор дополнения.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addition_id")
    @Getter
    @EqualsAndHashCode.Include
    private long Id;

    /**
     * Заголовок дополнения.
     */
    @Column(name = "addition_title")
    @Getter
    @Setter
    @NonNull
    @EqualsAndHashCode.Include
    private String title;

    /**
     * Текст дополнения.
     */
    @Column(name = "addition_description")
    @Getter
    @Setter
    @NonNull
    @EqualsAndHashCode.Include
    private String description;

    /**
     * Ссылка на GIF-анимацию к дополнению.
     */
    @Column(name = "addition_gif_link")
    @Getter
    @Setter
    private String gifLink;

    /**
     * Услуга, к которой относится дополнение.
     */
    @ManyToOne
    @JoinColumn(name = "item_id")
    @Getter
    @NonNull
    private Item item;
}
