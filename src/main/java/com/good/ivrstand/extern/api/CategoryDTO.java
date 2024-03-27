package com.good.ivrstand.extern.api;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO extends RepresentationModel<CategoryDTO> {

    private long id;

    @NonNull
    private String title;

    private List<Long> itemsInCategoryIds;
}