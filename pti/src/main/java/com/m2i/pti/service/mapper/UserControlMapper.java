package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.UserControlDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity UserControl and its DTO UserControlDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface UserControlMapper extends EntityMapper<UserControlDTO, UserControl> {

    

    

    default UserControl fromId(Long id) {
        if (id == null) {
            return null;
        }
        UserControl userControl = new UserControl();
        userControl.setId(id);
        return userControl;
    }
}
