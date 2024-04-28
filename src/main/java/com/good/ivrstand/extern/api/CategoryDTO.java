package com.good.ivrstand.extern.api;

import jakarta.persistence.Column;
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

    private long parentCategoryId;

    private List<Long> childrenCategoryIds;

    private String gifPreview;

    private String gifLink;
}