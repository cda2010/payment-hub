package com.m2i.pti.web.rest;


import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.InfoSupp;
import com.m2i.pti.service.InfosuppService;
import com.m2i.pti.service.dto.InfosuppDTO;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class InfosuppRessource {

    private final Logger log = LoggerFactory.getLogger(InfoSupp.class);
    private final InfosuppService infosuppService;

    public InfosuppRessource(InfosuppService infosuppService) {
        this.infosuppService = infosuppService;
    }

    @GetMapping("/infosupp")
    @Timed
    public ResponseEntity<List<InfosuppDTO>> getAllinfosupp(Pageable pageable) {
        log.debug("REST request to get a page of info");
        Page<InfosuppDTO> page = infosuppService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/infosuppall");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/infosupp/libelle")
    @Timed
    public String getInfoLibelle(@RequestParam("id") Long id) {
        log.debug("REST request to get a page of infoLIbelle");
        return  infosuppService.getInfoLibelle(id) ;
    }

    @PostMapping("/infosupp")
    public ResponseEntity<InfosuppDTO> create(@RequestBody @Validated InfosuppDTO infosuppDto) {
        log.debug("REST request to save Emetteur : {}", infosuppDto);
        InfosuppDTO supp = infosuppService.save(infosuppDto);
        return ResponseEntity.ok(supp) ;
    }

    @GetMapping("infosupp/{id}")
    public ResponseEntity<InfosuppDTO> findById(@PathVariable("id") Long id) {
        log.debug("REST request to get Emetteur : {}", id);
        InfosuppDTO infosuppDTO = infosuppService.findById(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(infosuppDTO));
    }

    @DeleteMapping("infosupp/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        infosuppService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("infosupp/{id}")
    public ResponseEntity<Void> update(@RequestBody @Validated InfosuppDTO infosuppDto, @PathVariable("id") Long id) {
        infosuppService.update(infosuppDto, id);
        return ResponseEntity.ok().build();
    }
}
