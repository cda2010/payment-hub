package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.RoleValidation;
import com.m2i.pti.domain.User;
import com.m2i.pti.domain.enumeration.Operateur;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.repository.RoleValidationRepository;
import com.m2i.pti.repository.UserRepository;
import com.m2i.pti.service.TypeServiceService;
import com.m2i.pti.service.dto.RoleValidationDTO;
import com.m2i.pti.service.dto.TypeServiceDTO;
import com.m2i.pti.service.dto.XTypeService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing TypeService.
 */
@RestController
@RequestMapping("/api")
public class TypeServiceResource {
  private final Logger log = LoggerFactory.getLogger(TypeServiceResource.class);

  private static final String ENTITY_NAME = "typeService";

  private final TypeServiceService typeServiceService;
  private final RoleValidationRepository roleValidationRepository;
  private final OperationRepository operationRepository;
  private final UserRepository userRepository;

  public TypeServiceResource(
    TypeServiceService typeServiceService,
    RoleValidationRepository roleValidationRepository,
    OperationRepository operationRepository,
    UserRepository userRepository
  ) {
    this.typeServiceService = typeServiceService;
    this.roleValidationRepository = roleValidationRepository;
    this.operationRepository = operationRepository;
    this.userRepository = userRepository;
  }

  /**
   * POST /type-services : Create a new typeService.
   *
   * @param typeServiceDTO the typeServiceDTO to create
   * @return the ResponseEntity with status 201 (Created) and with body the new
   *         typeServiceDTO, or with status 400 (Bad Request) if the typeService
   *         has already an ID
   * @throws URISyntaxException if the Location URI syntax is incorrect
   */
  @PostMapping("/type-services")
  @Timed
  public ResponseEntity<TypeServiceDTO> createTypeService(
    @Valid @RequestBody TypeServiceDTO typeServiceDTO
  )
    throws URISyntaxException {
    log.debug("REST request to save TypeService : {}", typeServiceDTO);
    if (typeServiceDTO.getId() != null) {
      throw new BadRequestAlertException(
        "A new typeService cannot already have an ID",
        ENTITY_NAME,
        "idexists"
      );
    }
    TypeServiceDTO result = typeServiceService.save(typeServiceDTO);
    return ResponseEntity
      .created(new URI("/api/type-services/" + result.getId()))
      .headers(
        HeaderUtil.createEntityCreationAlert(
          ENTITY_NAME,
          result.getId().toString()
        )
      )
      .body(result);
  }

  /**
   * PUT /type-services : Updates an existing typeService.
   *
   * @param typeServiceDTO the typeServiceDTO to update
   * @return the ResponseEntity with status 200 (OK) and with body the updated
   *         typeServiceDTO,
   *         or with status 400 (Bad Request) if the typeServiceDTO is not valid,
   *         or with status 500 (Internal Server Error) if the typeServiceDTO
   *         couldn't be updated
   * @throws URISyntaxException if the Location URI syntax is incorrect
   */
  @PutMapping("/type-services")
  @Timed
  public ResponseEntity<TypeServiceDTO> updateTypeService(
    @Valid @RequestBody TypeServiceDTO typeServiceDTO
  )
    throws URISyntaxException {
    log.debug("REST request to update TypeService : {}", typeServiceDTO);
    if (typeServiceDTO.getId() == null) {
      return createTypeService(typeServiceDTO);
    }
    TypeServiceDTO result = typeServiceService.save(typeServiceDTO);
    return ResponseEntity
      .ok()
      .headers(
        HeaderUtil.createEntityUpdateAlert(
          ENTITY_NAME,
          typeServiceDTO.getId().toString()
        )
      )
      .body(result);
  }

  /**
   * GET /type-services : get all the typeServices.
   *
   * @return the ResponseEntity with status 200 (OK) and the list of typeServices
   *         in body
   */
  /*
   * @GetMapping("/type-services")
   *
   * @Timed
   * public List<TypeServiceDTO> getListypeServices() {
   * log.debug("REST request to get list of TypeServices");
   * List<TypeServiceDTO> typeServiceDTOS = typeServiceService.findAll();
   * return typeServiceDTOS;
   * }
   */

  @GetMapping("/type-services")
  @Timed
  public ResponseEntity<List<TypeServiceDTO>> getListypeServices(
    @ApiParam Pageable pageable
  ) {
    log.debug("REST request to get list of TypeServices");
    Page<TypeServiceDTO> typeServiceDTOS = typeServiceService.findAll(pageable);
    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
      typeServiceDTOS,
      "/api/type-services"
    );
    return new ResponseEntity<>(
      typeServiceDTOS.getContent(),
      headers,
      HttpStatus.OK
    );
  }

  @GetMapping("/type-services/clientServices")
  @Timed
  public List<TypeServiceDTO> getServicesClient(Pageable pageable) {
    return typeServiceService.findServiceClient();
  }

  @GetMapping("/type-services/fraisServices")
  @Timed
  public List<TypeServiceDTO> getServicesWithFrais(Pageable pageable) {
    return typeServiceService.findServiceWithFrais();
  }

  /**
   * GET /type-services : get all the typeServices.
   *
   * @return the ResponseEntity with status 200 (OK) and the list of typeServices
   *         in body
   */
  @GetMapping("/type-services_list")
  @Timed
  public List<TypeServiceDTO> getAllTypeServices() {
    log.debug("REST request to get list of TypeServices");
    List<TypeServiceDTO> typeServiceDTOS = typeServiceService.findAll();
    return typeServiceDTOS;
  }

  @GetMapping("/type-services/someService")
  @Timed
  public List<TypeServiceDTO> getSomeTypeServices() {
    log.debug("REST request to get a page of TypeServices");
    List<TypeServiceDTO> typeServiceDTOList = typeServiceService.findSomeType();
    return typeServiceDTOList;
  }

  /**
   * GET /type-services/:id : get the "id" typeService.
   *
   * @param id the id of the typeServiceDTO to retrieve
   * @return the ResponseEntity with status 200 (OK) and with body the
   *         typeServiceDTO, or with status 404 (Not Found)
   */
  @GetMapping("/type-services/{id}")
  @Timed
  public ResponseEntity<TypeServiceDTO> getTypeService(@PathVariable Long id) {
    log.debug("REST request to get TypeService : {}", id);
    TypeServiceDTO typeServiceDTO = typeServiceService.findOne(id);
    return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typeServiceDTO));
  }

  /**
   * DELETE /type-services/:id : delete the "id" typeService.
   *
   * @param id the id of the typeServiceDTO to delete
   * @return the ResponseEntity with status 200 (OK)
   */
  @DeleteMapping("/type-services/{id}")
  @Timed
  public ResponseEntity<Void> deleteTypeService(@PathVariable Long id) {
    log.debug("REST request to delete TypeService : {}", id);
    typeServiceService.delete(id);
    return ResponseEntity
      .ok()
      .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
      .build();
  }

  @GetMapping("/type-services_list_optimise")
  @Timed
  public List<TypeServiceDTO> getAllTypeServicesOptimise() {
    log.debug("REST request to get list of TypeServices");
    return typeServiceService.findAllOptimise();
  }

  @GetMapping("/type-services/en-attente-par-role-validation/{login}")
  @Timed
  public ResponseEntity<List<XTypeService>> typeServiceEnAttenteValidation(
    @PathVariable String login
  ) {
    List<XTypeService> xListTypeService = new ArrayList<>();
    log.debug("REST request to get list of TypeServices by role validation");
    User user = userRepository.findByLogin(login);
    List<RoleValidation> listRolevalidation = roleValidationRepository.findAll();
    if (user != null) {
      Set<Authority> authorities = user.getAuthorities();
      for (RoleValidation roleValidation : listRolevalidation) {
        boolean checkPresent = typeServiceService.containsAuthority(
          authorities,
          roleValidation
        );
        if (checkPresent) {
          XTypeService xTypeService = new XTypeService();
          Long nbrOperation = (long) 0;

          switch (roleValidation.getOperateur()) {
            case EGALE:
              nbrOperation = operationRepository.getTotalOperationByTypeServiceAndMontantEquals(
                  roleValidation.getTypeService().getId(),
                  StatutOperation.ENATTENTE_VALIDATION,
                  roleValidation.getMontant()
                );
              break;
            case INFERIEUR_EGALE:
              nbrOperation = operationRepository.getTotalOperationByTypeServiceAndMontantINFEquals(
                  roleValidation.getTypeService().getId(),
                  StatutOperation.ENATTENTE_VALIDATION,
                  roleValidation.getMontant()
                );
              break;
            case SUPERIEUR_EGALE:
              nbrOperation = operationRepository.getTotalOperationByTypeServiceAndMontantSupEquals(
                  roleValidation.getTypeService().getId(),
                  StatutOperation.ENATTENTE_VALIDATION,
                  roleValidation.getMontant()
                );
              break;
            case INFERIEUR:
              nbrOperation = operationRepository.getTotalOperationByTypeServiceAndMontantINF(
                  roleValidation.getTypeService().getId(),
                  StatutOperation.ENATTENTE_VALIDATION,
                  roleValidation.getMontant()
                );
              break;
            case SUPERIEUR:
              nbrOperation = operationRepository.getTotalOperationByTypeServiceAndMontantSup(
                  roleValidation.getTypeService().getId(),
                  StatutOperation.ENATTENTE_VALIDATION,
                  roleValidation.getMontant()
                );
              break;
            default:
              log.debug("Aucun choix correspondant");
          }

          xTypeService.setTypeService(
            roleValidation.getTypeService().getLibelle()
          );
          xTypeService.setNombre(nbrOperation);
          xListTypeService.add(xTypeService);
        }
      }
    }
    System.out.println(
      "====================================" + xListTypeService
    );
    return new ResponseEntity<>(xListTypeService, HttpStatus.OK);
  }
}
