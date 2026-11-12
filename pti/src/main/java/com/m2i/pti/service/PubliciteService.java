package com.m2i.pti.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.m2i.pti.domain.Publicite;
import com.m2i.pti.repository.PubliciteRepository;
import com.m2i.pti.service.dto.PubliciteDTO;
import com.m2i.pti.service.mapper.PubliciteMapper;


/**
 * Service Implementation for managing Publicite.
 * Les images sont stockées directement en base (BLOB), pas dans MinIO.
 */
@Service
@Transactional
public class PubliciteService {

    private final Logger log = LoggerFactory.getLogger(PubliciteService.class);

    private final PubliciteRepository publiciteRepository;

    private final PubliciteMapper publiciteMapper;

    public PubliciteService(PubliciteRepository publiciteRepository, PubliciteMapper publiciteMapper) {
        this.publiciteRepository = publiciteRepository;
        this.publiciteMapper = publiciteMapper;
    }

    /**
     * Save a publicite. L'image est enregistrée en base (BLOB).
     *
     * @param publiciteDTO the entity to save
     * @param image        fichier image (optionnel pour mise à jour)
     * @return the persisted entity
     */
    public PubliciteDTO save(PubliciteDTO publiciteDTO, MultipartFile image) throws Exception {
        log.debug("Request to save Publicite : {}", publiciteDTO);
        Publicite publicite = publiciteMapper.toEntity(publiciteDTO);
        if (image != null && !image.isEmpty()) {
            publicite.setImageData(image.getBytes());
            String contentType = image.getContentType();
            publicite.setContentType(contentType != null ? contentType : "image/jpeg");
        }
        publicite = publiciteRepository.save(publicite);
        return publiciteMapper.toDto(publicite);
    }

    /**
     * Get all the publicite.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PubliciteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Publicite");
        return publiciteRepository.findAll(pageable)
            .map(publiciteMapper::toDto);
    }

    /**
     * Get one publicite by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PubliciteDTO findOne(Long id) {
        log.debug("Request to get Publicite : {}", id);
        Publicite publicite = publiciteRepository.findById(id).orElse(null);
        return publiciteMapper.toDto(publicite);
    }

    /**
     * Delete the publicite by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Publicite : {}", id);
        publiciteRepository.deleteById(id);
    }
}
