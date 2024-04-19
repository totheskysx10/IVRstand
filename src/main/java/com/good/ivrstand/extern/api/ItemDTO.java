package com.good.ivrstand.extern.api;

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

    private String gifLink;

    private long categoryId;

    private List<Long> additionIds;

    @NonNull
    private String keyWord;
}
