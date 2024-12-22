package com.good.ivrstand.extern.api.assembler;

import com.good.ivrstand.app.service.CategoryService;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import com.good.ivrstand.extern.api.controller.CategoryController;
import com.good.ivrstand.extern.api.dto.CategoryDTO;
import lombok.NonNull;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryAssembler extends RepresentationModelAssemblerSupport<Category, CategoryDTO> {

    public CategoryAssembler() {
        super(CategoryService.class, CategoryDTO.class);
    }

    @Override
    public @NonNull CategoryDTO toModel(@NonNull Category category) {
        CategoryDTO categoryDTO = instantiateModel(category);

        categoryDTO.setId(category.getId());
        categoryDTO.setTitle(category.getTitle());
        categoryDTO.setItemsInCategoryIds(category.getItemsInCategory().stream()
                .map(Item::getId)
                .collect(Collectors.toList()));
        if (category.getParentCategory() != null)
            categoryDTO.setParentCategoryId(category.getParentCategory().getId());
        categoryDTO.setChildrenCategoryIds(category.getChildrenCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList()));
        categoryDTO.setGifPreview(category.getGifPreview());
        categoryDTO.setGifLink(category.getGifLink());
        categoryDTO.setMainIconLink(category.getMainIconLink());
        categoryDTO.setTitleAudio(category.getTitleAudio());

        categoryDTO.add(linkTo(methodOn(CategoryController.class).getCategoryById(category.getId())).withSelfRel());

        return categoryDTO;
    }
}
