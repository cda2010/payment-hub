package com.gateway.payment.billing.api.dto;



public record BillSearchResponse(

        String searchMode,

        String query,

        java.util.List<BillDto> bills,

        int count

) {}


