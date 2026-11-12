package com.m2i.pti.web.rest;

import com.m2i.pti.domain.CategorieService;
import com.m2i.pti.repository.CategorieServiceRepository;
import com.m2i.pti.web.rest.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategorieServiceResource {

    private final CategorieServiceRepository repository;

    public CategorieServiceResource(CategorieServiceRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/categorie-service")
    public ResponseEntity<List<CategorieService>> list(Pageable pageable) {
        Page<CategorieService> page = repository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/categorie-service");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @PostMapping("/categorie-service")
    public CategorieService create(@RequestBody CategorieService body) {
        body.setId(null);
        return repository.save(body);
    }

    @PutMapping("/categorie-service")
    public CategorieService update(@RequestBody CategorieService body) {
        CategorieService current = repository.findById(body.getId()).orElseThrow();
        current.setCode(body.getCode());
        current.setLibelle(body.getLibelle());
        current.setOrdre(body.getOrdre());
        return repository.save(current);
    }

    @DeleteMapping("/categorie-service/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
