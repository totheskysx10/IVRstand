package com.good.ivrstand.extern.api.assembler;

import com.good.ivrstand.domain.Addition;
import com.good.ivrstand.domain.Item;
import com.good.ivrstand.extern.api.dto.ItemDTO;
import com.good.ivrstand.extern.api.controller.ItemController;
import lombok.NonNull;
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
    public @NonNull ItemDTO toModel(@NonNull Item item) {
        ItemDTO itemDTO = instantiateModel(item);

        itemDTO.setId(item.getId());
        itemDTO.setTitle(item.getTitle());
        itemDTO.setDescription(item.getDescription());
        itemDTO.setGifPreview(item.getGifPreview());
        itemDTO.setGifLink(item.getGifLink());
        if (item.getCategory() != null)
            itemDTO.setCategoryId(item.getCategory().getId());
        if (!item.getAdditions().isEmpty())
            itemDTO.setAdditionIds(item.getAdditions().stream()
                    .map(Addition::getId)
                    .collect(Collectors.toList()));
        itemDTO.setIconLinks(item.getIconLinks());
        itemDTO.setMainIconLink(item.getMainIconLink());
        itemDTO.setKeywords(item.getKeywords());
        itemDTO.setAudio(item.getAudio());
        itemDTO.setTitleAudio(item.getTitleAudio());

        itemDTO.add(linkTo(methodOn(ItemController.class).getItemById(item.getId())).withSelfRel());

        return itemDTO;
    }
}
