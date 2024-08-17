package com.good.ivrstand.extern.api.assembler;

import com.good.ivrstand.domain.Role;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.extern.api.dto.UserDTO;
import com.good.ivrstand.extern.api.controller.UserController;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserAssembler extends RepresentationModelAssemblerSupport<User, UserDTO> {

    public UserAssembler() {
        super(UserController.class, UserDTO.class);
    }
    @Override
    public UserDTO toModel(User user) {
        UserDTO userDTO = instantiateModel(user);

        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        userDTO.setEmailConfirmed(user.isEmailConfirmed());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());

        userDTO.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());

        return userDTO;
    }
}
