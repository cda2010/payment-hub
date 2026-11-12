package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.UserControl;
import com.m2i.pti.mobile.dto.UserExist;
import com.m2i.pti.service.UserControlService;
import com.m2i.pti.service.dto.BilanOperationDTO;
import com.m2i.pti.service.dto.UserControlDTO;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
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

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing UserControl.
 */
@RestController
@RequestMapping("/api")
public class UserControlResource {

    private final Logger log = LoggerFactory.getLogger(UserControlResource.class);

    private static final String ENTITY_NAME = "userControl";

    private final UserControlService userControlService;

    public UserControlResource(UserControlService userControlService) {
        this.userControlService = userControlService;
    }

    /**
     * POST  /user-controls : Create a new userControl.
     *
//     * @param userControlDTO the userControlDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new userControlDTO, or with status 400 (Bad Request) if the userControl has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
//    @PostMapping("/user-controls")
//    @Timed
//    public ResponseEntity<UserControlDTO> createUserControl(@RequestBody UserControlDTO userControlDTO) throws URISyntaxException {
//        log.debug("REST request to save UserControl : {}", userControlDTO);
//        if (userControlDTO.getId() != null) {
//            throw new BadRequestAlertException("A new userControl cannot already have an ID", ENTITY_NAME, "idexists");
//        }
//        UserControlDTO result = userControlService.save(userControlDTO);
//        return ResponseEntity.created(new URI("/api/user-controls/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
//            .body(result);
//    }
    @GetMapping("/user-controls/reset-password")
    @Timed
    public ResponseEntity<Void> resetPassword(@RequestParam(required = true) String telephone, @RequestParam(required = true) String login,@RequestParam(required = true)String codePays,@RequestParam(required = false)String typeActeur) {
        userControlService.resetPassword( telephone, login, codePays,typeActeur);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Mot de passe reinitialisé avec succès !",null)).build();
    }
    @GetMapping("/user-controls/disableUser")
    @Timed
    public ResponseEntity<Void> disableUser(@RequestParam(required = true) String login,@RequestParam(required = true)String codePays,@RequestParam(required = false)String typeActeur) {
        userControlService.disableUser( login, codePays,typeActeur);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Utilisateur désactivé avec succès !",null)).build();
    }
    @GetMapping("/user-controls/verification")
    @Timed
    public ResponseEntity<UserExist> verification(@RequestParam(required = true) String codePays,@RequestParam(required = true) String login, @RequestParam(required = false)String password) {

        UserExist userExist=userControlService.verification(login,codePays,password);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(userExist));
    }
    //
    /**
     * PUT  /user-controls : Updates an existing userControl.
     *
     * @param userControlDTO the userControlDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated userControlDTO,
     * or with status 400 (Bad Request) if the userControlDTO is not valid,
     * or with status 500 (Internal Server Error) if the userControlDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
//    @PutMapping("/user-controls")
//    @Timed
//    public ResponseEntity<UserControlDTO> updateUserControl(@RequestBody UserControlDTO userControlDTO) throws URISyntaxException {
//        log.debug("REST request to update UserControl : {}", userControlDTO);
//        if (userControlDTO.getId() == null) {
//            return createUserControl(userControlDTO);
//        }
//        UserControlDTO result = userControlService.save(userControlDTO);
//        return ResponseEntity.ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, userControlDTO.getId().toString()))
//            .body(result);
//    }

    /**
     * GET  /user-controls : get all the userControls.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of userControls in body
     */
    @GetMapping("/user-controls")
    @Timed
    public ResponseEntity<List<UserControlDTO>> getAllUserControls(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of UserControls");
        Page<UserControlDTO> page = userControlService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/user-controls");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /user-controls/:id : get the "id" userControl.
     *
     * @param id the id of the userControlDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the userControlDTO, or with status 404 (Not Found)
     */
    @GetMapping("/user-controls/{id}")
    @Timed
    public ResponseEntity<UserControlDTO> getUserControl(@PathVariable Long id) {
        log.debug("REST request to get UserControl : {}", id);
        UserControlDTO userControlDTO = userControlService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(userControlDTO));
    }

    /**
     * DELETE  /user-controls/:id : delete the "id" userControl.
     *
     * @param id the id of the userControlDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */

    @GetMapping("/user-controle/by-personne")
    @Timed
    public ResponseEntity<List<UserControlDTO>> getBilanOperationByPersonne(@RequestParam(required = true) Long personneId, @RequestParam(required = false) ZonedDateTime dateDebut, @RequestParam(required = false) ZonedDateTime dateFin, Pageable pageable ) {
        log.debug("Request to get user controle.......");
        Page<UserControlDTO> page=userControlService.findAllByPersonne(personneId,dateDebut,dateFin,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/user-controles");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

//
}
