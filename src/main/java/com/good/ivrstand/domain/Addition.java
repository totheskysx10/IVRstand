package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Сущность дополнения к услуге.
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
    @Column(name = "addition_title", columnDefinition = "TEXT")
    @Getter
    @Setter
    @NonNull
    @EqualsAndHashCode.Include
    private String title;

    /**
     * Текст дополнения.
     */
    @Column(name = "addition_description", columnDefinition = "TEXT")
    @Getter
    @Setter
    @NonNull
    @EqualsAndHashCode.Include
    private String description;

    /**
     * Ссылка на превью GIF-анимации дополненияю.
     */
    @Column(name = "addition_gif_preview")
    @Getter
    @Setter
    private String gifPreview;

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

    /**
     * Иконки к дополнению.
     */
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "addition_icons", joinColumns = @JoinColumn(name = "addition_id"))
    @Column(name = "icon_link")
    private List<String> iconLinks;

    /**
     * Ссылка на главную иконку, связанную с дополнением.
     */
    @Column(name = "addition_main_icon_link")
    @Getter
    @Setter
    private String mainIconLink;

    /**
     * Аудио описания дополнения.
     */
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "addition_audio", joinColumns = @JoinColumn(name = "addition_id"))
    @Column(name = "audio")
    private List<String> audio;

    /**
     * Аудио заголовка дополнения.
     */
    @Column(name = "addition_titleaudio")
    @Getter
    @Setter
    private String titleAudio;

    /**
     * Хэш описания.
     */
    @Column(name = "addition_deschash")
    @Getter
    @Setter
    private String descriptionHash;
}
