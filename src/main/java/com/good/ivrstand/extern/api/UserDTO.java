package com.good.ivrstand.extern.api;

import com.good.ivrstand.domain.UserRole;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
public class UserDTO extends RepresentationModel<UserDTO>  {
    private Long id;
    private String username;
    private String email;
    private List<UserRole> roles;
    private boolean emailConfirmed;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;
}
