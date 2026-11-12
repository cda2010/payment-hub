package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Compte;
import java.util.List;

public class ClientAllWithCreateAccountDTO {
    private ClientAllDTO clientAllDTO;
    private Long codeFse ;
    public ClientAllDTO getClientAllDTO() {
        return clientAllDTO;
    }
    public void setClientAllDTO(ClientAllDTO clientAllDTO) {
        this.clientAllDTO = clientAllDTO;
    }

    public Long getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(Long codeFse) {
        this.codeFse = codeFse;
    }
 

  
    
}
