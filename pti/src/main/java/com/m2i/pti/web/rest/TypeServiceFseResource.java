package com.m2i.pti.web.rest;

import com.m2i.pti.domain.TypeServiceFse;
import com.m2i.pti.repository.TypeServiceFseRepository;
import com.m2i.pti.repository.TypeServiceRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.web.rest.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TypeServiceFseResource {

    private final TypeServiceFseRepository repository;
    private final TypeServiceRepository typeServiceRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;

    public TypeServiceFseResource(TypeServiceFseRepository repository, TypeServiceRepository typeServiceRepository,
                                  UniteOrganisationRepository uniteOrganisationRepository) {
        this.repository = repository;
        this.typeServiceRepository = typeServiceRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
    }

    @GetMapping("/type-service-fse")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Row>> list(Pageable pageable) {
        Page<TypeServiceFse> page = repository.findAll(pageable);
        List<Row> rows = page.getContent().stream().map(this::toRow).toList();
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/type-service-fse");
        return new ResponseEntity<>(rows, headers, HttpStatus.OK);
    }

    @PostMapping("/type-service-fse")
    @Transactional
    public Row create(@RequestBody Row body) {
        body.id = null;
        return toRow(save(new TypeServiceFse(), body));
    }

    @PutMapping("/type-service-fse")
    @Transactional
    public Row update(@RequestBody Row body) {
        TypeServiceFse current = repository.findById(body.id).orElseThrow();
        return toRow(save(current, body));
    }

    @DeleteMapping("/type-service-fse/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private TypeServiceFse save(TypeServiceFse entity, Row body) {
        entity.setContextCodeFse(body.contextCodeFse);
        entity.setContextCodePv(body.contextCodePv);
        entity.setTypeService(body.typeServiceId == null ? null : typeServiceRepository.getReferenceById(body.typeServiceId));
        entity.setUniteOrganisation(body.uniteOrganisationId == null ? null : uniteOrganisationRepository.getReferenceById(body.uniteOrganisationId));
        return repository.save(entity);
    }

    private Row toRow(TypeServiceFse entity) {
        Row row = new Row();
        row.id = entity.getId();
        row.contextCodeFse = entity.getContextCodeFse();
        row.contextCodePv = entity.getContextCodePv();
        if (entity.getTypeService() != null) {
            row.typeServiceId = entity.getTypeService().getId();
            row.typeServiceLibelle = entity.getTypeService().getLibelle();
        }
        if (entity.getUniteOrganisation() != null) {
            row.uniteOrganisationId = entity.getUniteOrganisation().getId();
            row.libelleUniteOrganisation = entity.getUniteOrganisation().getLibelleUniteOrganisation();
        }
        return row;
    }

    public static class Row {
        public Long id;
        public String contextCodeFse;
        public String contextCodePv;
        public Long typeServiceId;
        public String typeServiceLibelle;
        public Long uniteOrganisationId;
        public String libelleUniteOrganisation;
    }
}
