package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Сущность услуги.
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

    /**
     * Иконки к услуге.
     */
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_icons", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "icon_link")
    private List<String> iconLinks;

    /**
     * Ссылка на главную иконку, связанную с услугой.
     */
    @Column(name = "main_icon_link")
    @Getter
    @Setter
    private String mainIconLink;

    /**
     * Ключевые слова к услуге.
     */
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_keywords", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    /**
     * Аудио описания услуги.
     */
    @Getter
    @Setter // TODO remove after DB adaptation
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_audio", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "audio")
    private List<String> audio;

    /**
     * Аудио заголовка услуги.
     */
    @Column(name = "item_titleaudio")
    @Getter
    @Setter
    private String titleAudio;

    /**
     * Хэш описания.
     */
    @Column(name = "item_deschash")
    @Getter
    @Setter
    private String descriptionHash;
}
