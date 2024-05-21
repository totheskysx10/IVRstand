package com.good.ivrstand.extern.api;

import com.good.ivrstand.domain.Addition;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ItemAssembler extends RepresentationModelAssemblerSupport<Item, ItemDTO> {

    public ItemAssembler() {
        super(ItemController.class, ItemDTO.class);
    }

    @Override
    public ItemDTO toModel(Item item) {
        ItemDTO itemDTO = instantiateModel(item);

        itemDTO.setId(item.getId());
        itemDTO.setTitle(item.getTitle());
        itemDTO.setDescription(item.getDescription());
        itemDTO.setGifPreview(item.getGifPreview());
        itemDTO.setGifLink(item.getGifLink());
        if (item.getCategory() != null)
            itemDTO.setCategoryId(item.getCategory().getId());
        if (item.getAdditions().size() != 0)
            itemDTO.setAdditionIds(item.getAdditions().stream()
                    .map(Addition::getId)
                    .collect(Collectors.toList()));

        itemDTO.add(linkTo(methodOn(ItemController.class).getItemById(item.getId())).withSelfRel());

        return itemDTO;
    }
}
