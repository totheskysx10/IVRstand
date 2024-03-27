package com.good.ivrstand.extern.api;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO extends RepresentationModel<ItemDTO> {

    private long id;

    @NonNull
    private String title;

    @NonNull
    private String description;

    @NonNull
    private String gifLink;

    private long categoryId;
}
