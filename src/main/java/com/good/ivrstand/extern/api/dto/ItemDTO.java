package com.good.ivrstand.extern.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO extends RepresentationModel<ItemDTO> {

    private long id;

    @NonNull
    private String title;

    @NonNull
    private String description;

    private String gifPreview;

    private String gifLink;

    private long categoryId;

    private List<Long> additionIds;

    private List<String> iconLinks;

    private String mainIconLink;

    private List<String> keywords;

    private List<String> audio;

    private String titleAudio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean enableAudio = false;
}
