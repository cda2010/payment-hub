package com.m2i.pti.repository;

import com.m2i.pti.service.dto.OperationCustom;

import java.util.List;

public interface OperationCustomRepositoryImpl {
    List<OperationCustom> findAll();
}
