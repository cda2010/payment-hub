package com.gateway.payment.configuration.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.api.dto.ServiceTypeResponse;
import com.gateway.payment.configuration.domain.ServiceType;
import com.gateway.payment.configuration.repository.ServiceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeService(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> listActive() {
        return serviceTypeRepository.findByActifTrueOrderByOrdreAffichageAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> listAll() {
        return serviceTypeRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getOrdreAffichage() != null ? a.getOrdreAffichage() : 0,
                        b.getOrdreAffichage() != null ? b.getOrdreAffichage() : 0))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<ServiceType> resolveEntities(List<ServiceTypeCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return new HashSet<>();
        }
        Set<ServiceTypeCode> unique = EnumSet.noneOf(ServiceTypeCode.class);
        unique.addAll(codes);
        return new HashSet<>(serviceTypeRepository.findAllById(unique));
    }

    private ServiceTypeResponse toResponse(ServiceType st) {
        return new ServiceTypeResponse(
                st.getCode(),
                st.getLibelle(),
                st.getCategory(),
                st.getDescription(),
                st.isActif(),
                st.getOrdreAffichage()
        );
    }
}
