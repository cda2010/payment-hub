package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TerminalDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Terminal} and its DTO {@link TerminalDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TerminalMapper extends EntityMapper<TerminalDTO, Terminal> {



    default Terminal fromId(Long id) {
        if (id == null) {
            return null;
        }
        Terminal terminal = new Terminal();
        terminal.setId(id);
        return terminal;
    }
}
