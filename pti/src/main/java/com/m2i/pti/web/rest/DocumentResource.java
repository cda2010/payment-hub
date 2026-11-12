package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Document;
import com.m2i.pti.repository.DocumentRepository;
import com.m2i.pti.service.DocumentService;
import com.m2i.pti.service.dto.ImageDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.DocumentDTO;
import io.minio.errors.*;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Document.
 */
@RestController
@RequestMapping("/api")
public class DocumentResource {

    private final Logger log = LoggerFactory.getLogger(DocumentResource.class);

    @Value("${m2i.security.pti.images.windows}")
    private String imagesPathWindows;

    @Value("${m2i.security.pti.images.linux}")
    private String imagesPathLinux;


    private static final String ENTITY_NAME = "document";

    private final DocumentService documentService;

    private final DocumentRepository documentRepository;

    public DocumentResource(DocumentService documentService, DocumentRepository documentRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
    }

    /**
     * POST  /documents : Create a new document.
     *
     * @param documentDTO the documentDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new documentDTO, or with status 400 (Bad Request) if the document has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/documents")
    @Timed
    public ResponseEntity<DocumentDTO> createDocument(@Valid @RequestBody DocumentDTO documentDTO) throws URISyntaxException {
        log.debug("REST request to save Document : {}", documentDTO);
        if (documentDTO.getId() != null) {
            throw new BadRequestAlertException("A new document cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DocumentDTO result = documentService.save(documentDTO);
        return ResponseEntity.created(new URI("/api/documents/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /documents : Updates an existing document.
     *
     * @param documentDTO the documentDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated documentDTO,
     * or with status 400 (Bad Request) if the documentDTO is not valid,
     * or with status 500 (Internal Server Error) if the documentDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/documents")
    @Timed
    public ResponseEntity<DocumentDTO> updateDocument(@Valid @RequestBody DocumentDTO documentDTO) throws URISyntaxException {
        log.debug("REST request to update Document : {}", documentDTO);
        if (documentDTO.getId() == null) {
            return createDocument(documentDTO);
        }
        DocumentDTO result = documentService.save(documentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, documentDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /documents : get all the documents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of documents in body
     */
    @GetMapping("/documents")
    @Timed
    public ResponseEntity<List<DocumentDTO>> getAllDocuments(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Documents");
        Page<DocumentDTO> page = documentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/documents");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /documents/:id : get the "id" document.
     *
     * @param id the id of the documentDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the documentDTO, or with status 404 (Not Found)
     */
    @GetMapping("/documents/{id}")
    @Timed
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id) {
        log.debug("REST request to get Document : {}", id);
        DocumentDTO documentDTO = documentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(documentDTO));
    }

    /**
     * DELETE  /documents/:id : delete the "id" document.
     *
     * @param id the id of the documentDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/documents/{id}")
    @Timed
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.debug("REST request to delete Document : {}", id);
        documentService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/document/showImage/old")
    @Timed
    public ImageDTO getImage(@RequestParam Long documentId) throws IOException {
        ImageDTO imageDTO=new ImageDTO();
        log.debug("*---------------"+documentId);
        Document document=new Document();
        String imageFolder=null;
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? imagesPathLinux : imagesPathWindows);
        byte[] bytes=null;
        if(documentId!=null){
            document=documentRepository.findById(documentId).orElse(null);
            if(document.getId()!=null){
                imageFolder=document.getImageFolder();
                
                File img = new File(imageFolder);
                log.debug("is file------"+img+"et bool"+img.isFile());
                bytes = FileUtils.readFileToByteArray(img);
                imageDTO.setImageFinal(bytes);
            }

        }
        return imageDTO;
    }

    @GetMapping("/document/showImage")
    @Timed
    public ImageDTO getImageFromMinio(@RequestParam Long documentId) throws IOException, InvalidResponseException, RegionConflictException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InvalidBucketNameException, InsufficientDataException, InternalException, InvalidExpiresRangeException {
        ImageDTO imageDTO=new ImageDTO();
        log.debug("*---------------"+documentId);
        Document document=new Document();
        String imageFolder=null;
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? imagesPathLinux : imagesPathWindows);
        byte[] bytes=null;
        if(documentId!=null){
            document=documentRepository.findById(documentId).orElse(null);
            if(document.getId()!=null){
                imageFolder=document.getImageFolder();
                String urlFileLink=documentService.getImageFromMinio(imageFolder);
                if(urlFileLink!=null &&!urlFileLink.equals("") ){
                    imageDTO.setImageLink(urlFileLink);
                }
                //File img = new File(imageFolder);
                //log.debug("is file------"+img+"et bool"+img.isFile());
                //bytes = FileUtils.readFileToByteArray(img);
                //imageDTO.setImageFinal(bytes);
            }

        }
        return imageDTO;
    }



}
