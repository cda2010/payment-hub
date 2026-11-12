package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.service.PersonneService;
import com.m2i.pti.service.dto.PersonneAllDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.PersonneDTO;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;

/**
 * REST controller for managing Personne.
 */
@RestController
@RequestMapping("/api")
public class PersonneResource {

    private final Logger log = LoggerFactory.getLogger(PersonneResource.class);

    private static final String ENTITY_NAME = "personne";

    private final PersonneService personneService;

    public PersonneResource(PersonneService personneService) {
        this.personneService = personneService;
    }

    /**
     * POST  /personnes : Create a new personne.
     *
     * @param personneDTO the personneDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new personneDTO, or with status 400 (Bad Request) if the personne has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/personnes")
    @Timed
    public ResponseEntity<PersonneDTO> createPersonne(@RequestBody PersonneDTO personneDTO) throws URISyntaxException {
        log.debug("REST request to save Personne : {}", personneDTO);
        if (personneDTO.getId() != null) {
            throw new BadRequestAlertException("A new personne cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PersonneDTO result = personneService.save(personneDTO);
        return ResponseEntity.created(new URI("/api/personnes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /personnes : Updates an existing personne.
     *
     * @param personneDTO the personneDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated personneDTO,
     * or with status 400 (Bad Request) if the personneDTO is not valid,
     * or with status 500 (Internal Server Error) if the personneDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/personnes")
    @Timed
    public ResponseEntity<PersonneDTO> updatePersonne(@RequestBody PersonneDTO personneDTO) throws URISyntaxException {
        log.debug("REST request to update Personne : {}", personneDTO);
        if (personneDTO.getId() == null) {
            return createPersonne(personneDTO);
        }
        PersonneDTO result = personneService.save(personneDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, personneDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /personnes : get all the personnes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of personnes in body
     */
    @GetMapping("/personnes")
    @Timed
    public ResponseEntity<List<PersonneDTO>> getAllPersonnes(Pageable pageable) {
        log.debug("REST request to get a page of Personnes");
        Page<PersonneDTO> page = personneService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/personnes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    /**
     * GET  /personnes/:id : get the "id" personne.
     *
     * @return the ResponseEntity with status 200 (OK) and with body the personneDTO, or with status 404 (Not Found)
     */
    @GetMapping("/personnes/recherche/byDocIdentification")
    @Timed
    public ResponseEntity<PersonneAllDTO> getPersonneByDocIdentification(@RequestParam(required = true) Long typeId, @RequestParam(required = true) String reference, @RequestParam(required = true) String telephone) {
        log.debug("REST request to get Personne : {}", typeId);
        PersonneAllDTO personneDTO = personneService.findByDocIdentification(typeId,reference,telephone);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(personneDTO));
    }


//    @GetMapping("/personnes/recherche/telephone")
//    @Timed
//    public List<PersonneAllDTO> getPersonneByDocIdentificatio(@RequestParam(required = false) String telephone,@RequestParam(required = false) String reference,@RequestParam(required = false) Long typeId,@RequestParam(required = false) String codeTelephone) {
//        
//        if (telephone!=null && telephone.length()>0){
//            return personneService.findByTelephone(telephone,typeId,reference,codeTelephone);
//        }else{
//            return personneService.findSansParamAll(typeId);
//        }
//        
//        
//    }
//    
     @GetMapping("/personnes/recherche/telephone")
    @Timed
    public ResponseEntity<List<PersonneAllDTO>>  getPersonneByDocIdentificatio(@ApiParam Pageable pageable,@RequestParam(required = false) String telephone,@RequestParam(required = false) String reference,@RequestParam(required = false) Long typeId,@RequestParam(required = false) String codeTelephone) {
         List<PersonneAllDTO> p;
        if (telephone!=null && telephone.length()>0){
            p= personneService.findByTelephone(telephone,typeId,reference,codeTelephone);
        }else{
            p= personneService.findSansParamAll(typeId);
        }
        
         Page<PersonneAllDTO> pages = new PageImpl<PersonneAllDTO>(p, pageable, p.size());
          HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(pages, "/api/personnes");
        return new ResponseEntity<>(pages.getContent(), headers, HttpStatus.OK);
    }
   

    /**
     * GET  /personnes/:id : get the "id" personne.
     *
     * @param id the id of the personneDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the personneDTO, or with status 404 (Not Found)
     */
    @GetMapping("/personnes/{id}")
    @Timed
    public ResponseEntity<PersonneDTO> getPersonne(@PathVariable Long id) {
        log.debug("REST request to get Personne : {}", id);
        PersonneDTO personneDTO = personneService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(personneDTO));
    }



    /**
     * DELETE  /personnes/:id : delete the "id" personne.
     *
     * @param id the id of the personneDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/personnes/{id}")
    @Timed
    public ResponseEntity<Void> deletePersonne(@PathVariable Long id) {
        log.debug("REST request to delete Personne : {}", id);
        personneService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/getDoublon")
    @Timed
    public ResponseEntity<List<PersonneDTO>> getDoublon(@ApiParam Pageable pageable,@RequestParam(required = false) Boolean hash,@RequestParam(required = false) Long typePieceId ) {
        System.out.println("------Get id---------hash--"+hash+"et-ty--"+typePieceId);
        if(hash==null){hash=false;}
        Page<PersonneDTO> pages=personneService.getDoublonNew(hash,typePieceId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(pages, "/api/liste-doublon");
        return new ResponseEntity<>(pages.getContent(), headers, HttpStatus.OK);
       // return personneService.saveDoublon(hash,typePieceId);
    }



    @GetMapping("/getInfosLien")
    @Timed
    public PersonneDTO getInfosLien(@RequestParam Long lienId){
      return personneService.getInfosLien(lienId);
    }

    @PostMapping("/personnes/getDoublouByPieceOrHash")
    @Timed
    public ResponseEntity<List<Personne>>  getDoublouByPieceOrHash(@ApiParam Pageable pageable,@RequestBody PersonneDTO personneDTO) {
        List<Personne> p;
        p= personneService.getDoublouByPieceorHash(personneDTO);
        Page<Personne> pages = new PageImpl<Personne>(p, pageable, p.size());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(pages, "/api/personnes");
        return new ResponseEntity<>(pages.getContent(), headers, HttpStatus.OK);
    }

}
