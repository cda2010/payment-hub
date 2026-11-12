package com.m2i.pti.service;

import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.RoleValidation;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.repository.TypeServiceRepository;
import com.m2i.pti.service.dto.TypeServiceDTO;
import com.m2i.pti.service.mapper.TypeServiceMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing TypeService.
 */
@Service
@Transactional
public class TypeServiceService {
  private final Logger log = LoggerFactory.getLogger(TypeServiceService.class);

  private final TypeServiceRepository typeServiceRepository;

  private final TypeServiceMapper typeServiceMapper;

  public TypeServiceService(
    TypeServiceRepository typeServiceRepository,
    TypeServiceMapper typeServiceMapper
  ) {
    this.typeServiceRepository = typeServiceRepository;
    this.typeServiceMapper = typeServiceMapper;
  }

  /**
   * Save a typeService.
   *
   * @param typeServiceDTO the entity to save
   * @return the persisted entity
   */
  public TypeServiceDTO save(TypeServiceDTO typeServiceDTO) {
    log.debug("Request to save TypeService : {}", typeServiceDTO);
    if (typeServiceDTO.getId() == null) {
      verifCode(typeServiceDTO);
      verifLibelle(typeServiceDTO);
      TypeService typeService = typeServiceMapper.toEntity(typeServiceDTO);
      typeService = typeServiceRepository.save(typeService);
      return typeServiceMapper.toDto(typeService);
    } else {
      TypeService typeServiceExistant = typeServiceRepository.findById(
        typeServiceDTO.getId()
      ).orElse(null);
      if (!typeServiceExistant.getCode().equals(typeServiceDTO.getCode())) {
        verifCode(typeServiceDTO);
      }
      if (
        !typeServiceExistant.getLibelle().equals(typeServiceDTO.getLibelle())
      ) {
        verifLibelle(typeServiceDTO);
      }
      TypeService typeService = typeServiceMapper.toEntity(typeServiceDTO);
      typeService = typeServiceRepository.save(typeService);
      return typeServiceMapper.toDto(typeService);
    }
  }

  private void verifLibelle(TypeServiceDTO typeServiceDTO) {
    if (typeServiceRepository.existsByLibelle(typeServiceDTO.getLibelle())) {
      throw new CustomParameterizedException("Le libelle existe déjà !");
    }
  }

  private void verifCode(TypeServiceDTO typeServiceDTO) {
    if (typeServiceRepository.existsByCode(typeServiceDTO.getCode())) {
      throw new CustomParameterizedException("Le code existe déjà !");
    }
  }

  /**
   * Get all the typeServices.
   *
   * @param pageable the pagination information
   * @return the list of entities
   */
  @Transactional(readOnly = true)
  public Page<TypeServiceDTO> findAll(Pageable pageable) {
    log.debug("Request to get all TypeServices");
    return typeServiceRepository
      .findAll(pageable)
      .map(typeServiceMapper::toDto);
  }

  /**
   * Get all the typeServices.
   *
   * @return the list of entities
   */
  @Transactional(readOnly = true)
  public List<TypeServiceDTO> findAll() {
    log.debug("Request to get all TypeServices");
    return typeServiceMapper.toDto(typeServiceRepository.findAll());
  }

  @Transactional(readOnly = true)
  public List<TypeServiceDTO> findSomeType() {
    log.debug("Request to get all TypeServices");
    return typeServiceMapper.toDto(typeServiceRepository.getSomeTypeService());
  }

  public TypeService findByService(String typeService) {
    return typeServiceMapper.toEntity(
      typeServiceRepository.findByCode(typeService)
    );
  }
 public TypeService findService(String typeService) {
    return typeServiceMapper.toEntity(
      typeServiceRepository.findByServiceCode(typeService)
    );
  }

  public TypeService findByCodeService(String typeService) {
    return typeServiceRepository.findTopByCode(typeService);
  }

  public List<TypeServiceDTO> findServiceClient() {
    return typeServiceMapper.toDto(
      typeServiceRepository.findByOperationClientTrue()
    );
  }

  public List<TypeServiceDTO> findServiceWithFrais() {
    return typeServiceMapper.toDto(
      typeServiceRepository.findByFraisApplicableTrue()
    );
  }

  /**
   * Get one typeService by id.
   *
   * @param id the id of the entity
   * @return the entity
   */
  @Transactional(readOnly = true)
  public TypeServiceDTO findOne(Long id) {
    log.debug("Request to get TypeService : {}", id);
    TypeService typeService = typeServiceRepository.findById(id).orElse(null);
    return typeServiceMapper.toDto(typeService);
  }

  /**
   * Delete the typeService by id.
   *
   * @param id the id of the entity
   */
  public void delete(Long id) {
    log.debug("Request to delete TypeService : {}", id);
    typeServiceRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<TypeServiceDTO> findAllOptimise() {
    log.debug("Request to get all TypeServices");
    return typeServiceRepository.findAllTypeService();
  }

  public boolean containsAuthority(
    Set<Authority> authorities,
    RoleValidation roleValidation
  ) {
    return authorities
      .stream()
      .anyMatch(auth -> auth.equals(roleValidation.getRole()));
  }
}
