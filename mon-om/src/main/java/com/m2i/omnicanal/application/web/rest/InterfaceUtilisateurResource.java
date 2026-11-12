package com.m2i.omnicanal.application.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.InterfaceUser;
import com.m2i.omnicanal.application.domain.ParametreMethod;
import com.m2i.omnicanal.application.domain.ParametreUser;
import com.m2i.omnicanal.application.repository.InterfaceUtilisateurRepository;
import com.m2i.omnicanal.application.service.InterfaceUtilisateurService;
import com.m2i.omnicanal.application.service.ParametreMethodeService;
import com.m2i.omnicanal.application.service.ParametreUtiliateurService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import com.m2i.omnicanal.model.enumeration.ParamatreType;
import com.m2i.omnicanal.model.enumeration.TypeEnvoi;

import io.github.jhipster.web.util.ResponseUtil;


/**
 * REST controller for managing InterfaceUtilisateur.
 */
@RestController
@RequestMapping("/api")
public class InterfaceUtilisateurResource {

    private final Logger log = LoggerFactory.getLogger(InterfaceUtilisateurResource.class);

    private static final String ENTITY_NAME = "interfaceUtilisateur";

    private final InterfaceUtilisateurService interfaceUtilisateurService;

    private final InterfaceUtilisateurRepository IURepository;
   private final ParametreMethodeService parametreMethodeService;
      private final    ParametreUtiliateurService parametreUtilisateurService;
    public InterfaceUtilisateurResource(ParametreMethodeService parametreMethodeService, InterfaceUtilisateurService interfaceUtilisateurService, InterfaceUtilisateurRepository iuRepository, ParametreUtiliateurService parametreUtilisateurService) {
        this.interfaceUtilisateurService = interfaceUtilisateurService;
        IURepository = iuRepository;
        this.parametreUtilisateurService=parametreUtilisateurService;
        this.parametreMethodeService=parametreMethodeService;
    }

    /**
     * POST  /interface-utilisateurs : Create a new interfaceUtilisateur.
     *
     * @param interfaceUtilisateur the interfaceUtilisateur to create
     * @return the ResponseEntity with status 201 (Created) and with body the new interfaceUtilisateur, or with status 400 (Bad Request) if the interfaceUtilisateur has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/interface-utilisateurs")
    @Timed
    public ResponseEntity<InterfaceUser> createInterfaceUtilisateur(@RequestBody InterfaceUser interfaceUtilisateur) throws URISyntaxException {
        log.debug("REST request to save InterfaceUtilisateur : {}", interfaceUtilisateur);
        if (interfaceUtilisateur.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new interfaceUtilisateur cannot already have an ID")).body(null);
        }
        InterfaceUser result = interfaceUtilisateurService.save(interfaceUtilisateur);
        if (result.getTypeInterface().equals(TypeEnvoi.REQUEST)){
            ParametreUser paramUser = new ParametreUser ();
            paramUser.setCodeParametre("CODEFSESOURCE");
            paramUser.setNomParametre("Fse Source");
            paramUser.setInterfaceUtilisateur(result);
            paramUser.setTypeParametre(ParamatreType.STRING);
            parametreUtilisateurService.save(paramUser);
            paramUser = new ParametreUser ();
              paramUser.setInterfaceUtilisateur(result);
            paramUser.setCodeParametre("CODEFSEDEST");
            paramUser.setNomParametre("Fse Destination");
            paramUser.setTypeParametre(ParamatreType.STRING);
            parametreUtilisateurService.save(paramUser);
      //      interfaceUtilisateur.getMethode().getId()
             List<ParametreMethod> listParamMethode = parametreMethodeService.findByMethode(interfaceUtilisateur.getMethode().getId());
             
             for(ParametreMethod parametreMethod:listParamMethode){
                     paramUser = new ParametreUser ();
                     paramUser.setCodeParametre(parametreMethod.getCodeParametre());
                     paramUser.setNomParametre(parametreMethod.getNomParametre());
                     paramUser.setInterfaceUtilisateur(result);
                     paramUser.setTypeParametre(parametreMethod.getTypeParametre());
                     parametreUtilisateurService.save(paramUser);
             }
              
        }
        return ResponseEntity.created(new URI("/api/interface-utilisateurs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /interface-utilisateurs : Updates an existing interfaceUtilisateur.
     *
     * @param interfaceUtilisateur the interfaceUtilisateur to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated interfaceUtilisateur,
     * or with status 400 (Bad Request) if the interfaceUtilisateur is not valid,
     * or with status 500 (Internal Server Error) if the interfaceUtilisateur couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/interface-utilisateurs")
    @Timed
    public ResponseEntity<InterfaceUser> updateInterfaceUtilisateur(@RequestBody InterfaceUser interfaceUtilisateur) throws URISyntaxException {
        log.debug("REST request to update InterfaceUtilisateur : {}", interfaceUtilisateur);
        if (interfaceUtilisateur.getId() == null) {
            return createInterfaceUtilisateur(interfaceUtilisateur);
        }
        InterfaceUser result = interfaceUtilisateurService.save(interfaceUtilisateur);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, interfaceUtilisateur.getId().toString()))
            .body(result);
    }

    /**
     * GET  /interface-utilisateurs : get all the interfaceUtilisateurs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of interfaceUtilisateurs in body
     */
    @GetMapping("/interface-utilisateurs")
    @Timed
//    public List<InterfaceUser> getAllInterfaceUtilisateurs() {
//        log.debug("REST request to get all InterfaceUtilisateurs");
//        return interfaceUtilisateurService.findAll();
//    }
    public ResponseEntity<List<InterfaceUser>> getAllInterfaceUtilisateurs(Pageable pageable) {
        log.debug("REST request to get all InterfaceUtilisateurs");
        Page<InterfaceUser> page = IURepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/apis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

    /**
     * GET  /interface-utilisateurs/:id : get the "id" interfaceUtilisateur.
     *
     * @param id the id of the interfaceUtilisateur to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the interfaceUtilisateur, or with status 404 (Not Found)
     */
    @GetMapping("/interface-utilisateurs/{id}")
    @Timed
    public ResponseEntity<InterfaceUser> getInterfaceUtilisateur(@PathVariable Long id) {
        log.debug("REST request to get InterfaceUtilisateur : {}", id);
        InterfaceUser interfaceUtilisateur = interfaceUtilisateurService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(interfaceUtilisateur));
    }

    /**
     * DELETE  /interface-utilisateurs/:id : delete the "id" interfaceUtilisateur.
     *
     * @param id the id of the interfaceUtilisateur to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/interface-utilisateurs/{id}")
    @Timed
    public ResponseEntity<Void> deleteInterfaceUtilisateur(@PathVariable Long id) {
        log.debug("REST request to delete InterfaceUtilisateur : {}", id);
        interfaceUtilisateurService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/interface-utilisateurs/filtered")
    @Timed
    public List<InterfaceUser> getAllFilteredOperations(@RequestParam(required = false) String ptypeMethode,
                                                        @RequestParam(required = false) String pfse) {
        log.debug("\n\n\n-------------------------------: {},{},{},{},{},{}",ptypeMethode,pfse);
        return IURepository.rechercheIU(ptypeMethode,pfse);
    }

    @PostMapping("/interface-utilisateurs/{id}/duplicate")
    @Timed
    public ResponseEntity<InterfaceUser> duplicateInterfaceUtilisateur(@PathVariable Long id, @RequestBody InterfaceUser newInterfaceUtilisateur) throws URISyntaxException {
        InterfaceUser originalInterfaceUtilisateur = interfaceUtilisateurService.findOne(id);
        if (originalInterfaceUtilisateur == null) {
            return ResponseEntity.notFound().build();
        }
        log.debug("REST request to duplicate InterfaceUtilisateur : {}", newInterfaceUtilisateur);
        newInterfaceUtilisateur.setId(null);
      
      //  interfaceUtilisateur.setParametreUtilisateur(originalInterfaceUtilisateur.getParametreUtilisateur());
        InterfaceUser result = interfaceUtilisateurService.duplicate(originalInterfaceUtilisateur,newInterfaceUtilisateur);

        return ResponseEntity.created(new URI("/api/interface-utilisateurs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
}
