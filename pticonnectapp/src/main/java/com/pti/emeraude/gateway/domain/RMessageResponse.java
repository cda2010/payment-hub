package com.pti.emeraude.gateway.domain;

import java.util.List;

public class RMessageResponse {

    private long totalElements;

    private List<MOperatDTO> mOperationDTOList;

    public List<MOperatDTO> getmOperationDTOList() {
        return mOperationDTOList;
    }

    public void setmOperationDTOList(List<MOperatDTO> mOperationDTOList) {
        this.mOperationDTOList = mOperationDTOList;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
