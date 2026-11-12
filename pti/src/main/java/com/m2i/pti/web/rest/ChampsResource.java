package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Champs;

import com.m2i.pti.domain.Composant;
import com.m2i.pti.domain.SelectComponentData;
import com.m2i.pti.repository.ChampsRepository;
import com.m2i.pti.service.dto.ChampsListeDTO;
import com.m2i.pti.repository.ComposantRepository;
import com.m2i.pti.repository.SelectComponentDataRepository;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Champs.
 */
@RestController
@RequestMapping("/api")
public class ChampsResource {

    private final Logger log = LoggerFactory.getLogger(ChampsResource.class);

    private static final String ENTITY_NAME = "champs";

    private final ChampsRepository champsRepository;

    private final ComposantRepository composantRepository;

    private final SelectComponentDataRepository selectComponentDataRepository;

    public ChampsResource(ChampsRepository champsRepository, ComposantRepository composantRepository, SelectComponentDataRepository selectComponentDataRepository) {
        this.champsRepository = champsRepository;
        this.composantRepository = composantRepository;
        this.selectComponentDataRepository = selectComponentDataRepository;
    }

    /**
     * POST  /champs : Create a new champs.
     *
     * @param champs the champs to create
     * @return the ResponseEntity with status 201 (Created) and with body the new champs, or with status 400 (Bad Request) if the champs has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/champs")
    @Timed
    public ResponseEntity<Champs> createChamps(@RequestBody Champs champs) throws URISyntaxException {
        log.debug("REST request to save Champs : {}", champs);
        if (champs.getId() != null) {
            throw new BadRequestAlertException("A new champs cannot already have an ID", ENTITY_NAME, "idexists");
        }
        champs.setDefaultValue(champs.getComposant().getDefaultValue());
        log.debug("--------------1::::"+champs.getComposant().getId()+"champ id"+champs.getId());

        if(champs.getComposant()!=null){
            Composant composant=composantRepository.findById(champs.getComposant().getId()).orElse(null);
            if(composant!=null) {
                log.debug("--------------1"+composant.getId()+"et-----"+composant.getTypeComposant());
                if (composant.getListe() != null) {
                    if (composant.getListe().getEstStatic() != null && !composant.getListe().getEstStatic()) {
                        SelectComponentData selectComponentData = new SelectComponentData();
                        if (champs.getSelectComponentData().getData() != null) {
                            log.debug("--------------1" + champs.getSelectComponentData().getData());
                            selectComponentData.setData(champs.getSelectComponentData().getData());
                            selectComponentData = selectComponentDataRepository.save(selectComponentData);
                            if (selectComponentData.getId() != null) {
                                log.debug("--------------1" + selectComponentData);
                                champs.setSelectComponentData(selectComponentData);
                            }
                        }

                    }
                }
            }
        }
        Champs result = champsRepository.save(champs);
        return ResponseEntity.created(new URI("/api/champs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /champs : Updates an existing champs.
     *
     * @param champs the champs to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated champs,
     * or with status 400 (Bad Request) if the champs is not valid,
     * or with status 500 (Internal Server Error) if the champs couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/champs")
    @Timed
    public ResponseEntity<Champs> updateChamps(@RequestBody Champs champs) throws URISyntaxException {
        log.debug("REST request to update Champs : {}", champs);

        if (champs.getId() == null) {
            return createChamps(champs);
        }
        //champs.setDefaultValue(champs.getComposant().getDefaultValue());
        if(champs.getSelectComponentData()!=null&&champs.getSelectComponentData().getId()!=null){
                SelectComponentData selectComponentData= selectComponentDataRepository.save(champs.getSelectComponentData());
                if(selectComponentData.getId()!=null){
                    champs.setSelectComponentData(selectComponentData);
                }
        } else {
            if(champs.getComposant()!=null){
                if(champs.getComposant().getListe()!=null){
                    if(champs.getComposant().getListe().getEstStatic()!=null &&!champs.getComposant().getListe().getEstStatic()){
                        SelectComponentData selectComponentData=new SelectComponentData();
                        selectComponentData.setData(champs.getSelectComponentData().getData());
                        selectComponentData=selectComponentDataRepository.save(selectComponentData);
                        if(selectComponentData!=null){
                            champs.setSelectComponentData(selectComponentData);
                        }
                    }
                }
            }
        }

        Champs result = champsRepository.save(champs);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, champs.getId().toString()))
            .body(result);
    }

    /**
     * GET  /champs : get all the champs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of champs in body
     */
    @GetMapping("/champs")
    @Timed
    public List<Champs> getAllChamps() {
        log.debug("REST request to get all Champs");
        return champsRepository.findAll();
        }

    /**
     * GET  /champs/:id : get the "id" champs.
     *
     * @param id the id of the champs to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the champs, or with status 404 (Not Found)
     */
    @GetMapping("/champs/{id}")
    @Timed
    public ResponseEntity<Champs> getChamps(@PathVariable Long id) {
        log.debug("REST request to get Champs : {}", id);
        Champs champs = champsRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(champs));
    }
    @GetMapping("/champs/of-ecran/{id}/liste")
    @Timed
    public List<ChampsListeDTO> getChampsListe(@PathVariable Long id) {
        return champsRepository.findListeByEcran(id);
    }

    @GetMapping("/champs/of-ecran/{id}")
    @Timed
    public List<Champs> getChampsOfEcran(@PathVariable Long id) {

        return champsRepository.findAllByEcranId(id);
    }

    /**
     * DELETE  /champs/:id : delete the "id" champs.
     *
     * @param id the id of the champs to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/champs/{id}")
    @Timed
    public ResponseEntity<Void> deleteChamps(@PathVariable Long id) {
        log.debug("REST request to delete Champs : {}", id);
        champsRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
