package com.good.ivrstand.extern.api.assembler;

import com.good.ivrstand.domain.Addition;
import com.good.ivrstand.extern.api.controller.AdditionController;
import com.good.ivrstand.extern.api.dto.AdditionDTO;
import lombok.NonNull;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AdditionAssembler extends RepresentationModelAssemblerSupport<Addition, AdditionDTO> {

    public AdditionAssembler() {
        super(AdditionController.class, AdditionDTO.class);
    }

    @Override
    public @NonNull AdditionDTO toModel(@NonNull Addition addition) {
        AdditionDTO additionDTO = instantiateModel(addition);

        additionDTO.setId(addition.getId());
        additionDTO.setTitle(addition.getTitle());
        additionDTO.setDescription(addition.getDescription());
        additionDTO.setGifPreview(addition.getGifPreview());
        additionDTO.setGifLink(addition.getGifLink());
        additionDTO.setItemId(addition.getItem().getId());
        additionDTO.setIconLinks(addition.getIconLinks());
        additionDTO.setMainIconLink(addition.getMainIconLink());
        additionDTO.setAudio(addition.getAudio());
        additionDTO.setTitleAudio(addition.getTitleAudio());

        additionDTO.add(linkTo(methodOn(AdditionController.class).getAdditionById(addition.getId())).withSelfRel());

        return additionDTO;
    }
}
