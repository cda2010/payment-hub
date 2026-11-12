package com.m2i.pti.web.rest;

import com.m2i.pti.domain.GroupeFse;
import com.m2i.pti.repository.GroupeFseRepository;
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
public class GroupeFseResource {

    private final GroupeFseRepository repository;

    public GroupeFseResource(GroupeFseRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/groupe-fse")
    public ResponseEntity<List<GroupeFse>> list(Pageable pageable) {
        Page<GroupeFse> page = repository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/groupe-fse");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @PostMapping("/groupe-fse")
    public GroupeFse create(@RequestBody GroupeFse body) {
        body.setId(null);
        return repository.save(body);
    }

    @PutMapping("/groupe-fse")
    public GroupeFse update(@RequestBody GroupeFse body) {
        GroupeFse current = repository.findById(body.getId()).orElseThrow();
        current.setCode(body.getCode());
        current.setLibelle(body.getLibelle());
        return repository.save(current);
    }

    @DeleteMapping("/groupe-fse/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
