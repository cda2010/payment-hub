package com.m2i.pti.service.dto;


import java.util.List;

public class PointVenteUserDTO{
    private List<Long> userIds;

    private Long pointVenteId;


    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Long getPointVenteId() {
        return pointVenteId;
    }

    public void setPointVenteId(Long pointVenteId) {
        this.pointVenteId = pointVenteId;
    }
}
