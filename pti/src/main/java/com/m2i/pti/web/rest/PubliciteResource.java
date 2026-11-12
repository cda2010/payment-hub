package com.m2i.pti.web.rest;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Pays;
import com.m2i.pti.domain.Publicite;
import com.m2i.pti.repository.PubliciteRepository;
import com.m2i.pti.service.PaysService;
import com.m2i.pti.service.PubliciteService;
import com.m2i.pti.service.dto.PubliciteDTO;
import com.m2i.pti.service.mapper.PaysMapper;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;
import java.time.LocalDateTime;


/**
 * REST controller for managing Publicite.
 */
@RestController
@RequestMapping("/api")
public class PubliciteResource {

    private final Logger log = LoggerFactory.getLogger(PubliciteResource.class);

    private static final String ENTITY_NAME = "publicite";

    private final PubliciteService publiciteService;
    private final PaysService paysService;
    private final PaysMapper paysMapper;
    private final PubliciteRepository publiciteRepository;

    public PubliciteResource(PubliciteService publiciteService, PaysService paysService, PaysMapper paysMapper,
                             PubliciteRepository publiciteRepository) {
        this.publiciteService = publiciteService;
        this.paysService = paysService;
        this.paysMapper = paysMapper;
        this.publiciteRepository = publiciteRepository;
    }

    /**
     * POST  /publicite : Create a new publicite.
     *
     * @param publiciteDTO the publiciteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new publiciteDTO, or with status 400 (Bad Request) if the publicite has already an ID
          * @throws Exception 
          */
         @PostMapping("/publicite")
         @Timed
         public ResponseEntity<PubliciteDTO> createPublicte(
            @RequestParam(value = "urlMinioPublicite") MultipartFile urlMinioPublicite,  
            @RequestParam("pays") Long pays, 
            @RequestParam("dateDebut") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam("dateFin")  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin,
            @RequestParam("frequence") Integer frequence)
          throws Exception  {
 //           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)");
           // LocalDate dateDebut = LocalDate.parse(dateDebutT, formatter);
           // LocalDate dateFin = LocalDate.parse(dateFinT, formatter);
        Pays paysId = paysMapper.toEntity(paysService.findOne(pays));
        PubliciteDTO publiciteDTO = new PubliciteDTO();
        publiciteDTO.setDateDebut(dateDebut);
        publiciteDTO.setDateFin(dateFin);
        publiciteDTO.setFrequence(frequence);
        publiciteDTO.setPays(paysId);
        log.debug("REST request to save Publicite : {}", publiciteDTO);
        if (publiciteDTO.getId() != null) {
            throw new BadRequestAlertException("A new publicite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PubliciteDTO result = publiciteService.save(publiciteDTO,urlMinioPublicite);
        return ResponseEntity.created(new URI("/api/publicite/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /publicite : Updates an existing publicite.
     *
     * @param publiciteDTO the publiciteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated publiciteDTO,
     * or with status 400 (Bad Request) if the publiciteDTO is not valid,
     * or with status 500 (Internal Server Error) if the publiciteDTO couldn't be updated
     * @throws Exception 
     */
    @PutMapping("/publicite")
    @Timed
    public ResponseEntity<PubliciteDTO> updatePays(@Valid @RequestBody PubliciteDTO publiciteDTO) throws Exception {
        log.debug("REST request to update Publicite : {}", publiciteDTO);
        if (publiciteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Publicite current = publiciteRepository.findById(publiciteDTO.getId()).orElse(null);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }
        current.setDateDebut(publiciteDTO.getDateDebut());
        current.setDateFin(publiciteDTO.getDateFin());
        current.setFrequence(publiciteDTO.getFrequence());
        current.setActif(publiciteDTO.getActif());
        if (publiciteDTO.getPaysId() != null) {
            current.setPays(paysMapper.toEntity(paysService.findOne(publiciteDTO.getPaysId())));
        }
        publiciteRepository.save(current);
        return ResponseEntity.ok(publiciteService.findOne(current.getId()));
    }

    /**
     * GET  /publicite : get all the publicite.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of publicite in body
     */
    @GetMapping("/publicite")
    @Timed
    public ResponseEntity<List<PubliciteDTO>> getAllPays(Pageable pageable) {
        log.debug("REST request to get a page of Publicite");
        Page<PubliciteDTO> page = publiciteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/publicite");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/publicite-list")
    @Timed
    public ResponseEntity<List<PubliciteDTO>> getAllPaysAl(Pageable pageable) {
        log.debug("REST request to get a page of Publicite");
        Page<PubliciteDTO> page = publiciteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/publicite");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    /**
     * GET  /publicite/:id : get the "id" publicite.
     *
     * @param id the id of the publiciteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the publiciteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/publicite/{id}")
    @Timed
    public ResponseEntity<PubliciteDTO> getPays(@PathVariable Long id) {
        log.debug("REST request to get Publicite : {}", id);
        PubliciteDTO publiciteDTO = publiciteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(publiciteDTO));
    }

    /**
     * GET  /publicite/image/:id : retourne l'image de la publicité (stockée en base).
     */
    @GetMapping("/publicite/image/{id}")
    @Timed
    public ResponseEntity<byte[]> getPubliciteImage(@PathVariable Long id) {
        log.debug("REST request to get Publicite image : {}", id);
        Publicite publicite = publiciteRepository.findById(id).orElse(null);
        if (publicite == null || publicite.getImageData() == null || publicite.getImageData().length == 0) {
            return ResponseEntity.notFound().build();
        }
        String contentType = publicite.getContentType() != null ? publicite.getContentType() : "image/jpeg";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(publicite.getImageData());
    }

    /**
     * DELETE  /publicite/:id : delete the "id" publicite.
     *
     * @param id the id of the publiciteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/publicite/{id}")
    @Timed
    public ResponseEntity<Void> deletePays(@PathVariable Long id) {
        log.debug("REST request to delete Publicite : {}", id);
        publiciteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
