package com.good.ivrstand.extern.api;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionDTO extends RepresentationModel<AdditionDTO> {
    private long id;

    @NonNull
    private String title;

    @NonNull
    private String description;

    private String gifLink;

    private long itemId;
}