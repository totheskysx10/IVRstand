package com.good.ivrstand.extern.api;

import com.good.ivrstand.domain.Addition;
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
    public AdditionDTO toModel(Addition addition) {
        AdditionDTO additionDTO = instantiateModel(addition);

        additionDTO.setId(addition.getId());
        additionDTO.setTitle(addition.getTitle());
        additionDTO.setDescription(addition.getDescription());
        additionDTO.setGifLink(addition.getGifLink());
        additionDTO.setItemId(addition.getItem().getId());

        additionDTO.add(linkTo(methodOn(AdditionController.class).getAdditionById(addition.getId())).withSelfRel());

        return additionDTO;
    }
}
