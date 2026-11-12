package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.FileExport;
import com.m2i.pti.domain.FileExportParam;
import com.m2i.pti.domain.ParamValues;
import com.m2i.pti.repository.FileExportParamRepository;
import com.m2i.pti.repository.FileExportRepository;
import com.m2i.pti.service.FileExportParamService;
import com.m2i.pti.service.FileExportService;
import com.m2i.pti.service.dto.FileExportDTO;
import com.m2i.pti.service.pentaho.ExecuteTransformation;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing FileExport.
 */
@RestController
@RequestMapping("/api")
public class FileExportResource {
  private final Logger log = LoggerFactory.getLogger(FileExportResource.class);

  private static final String ENTITY_NAME = "fileExport";

  private final FileExportService fileExportService;
  private final FileExportParamService fileExportParamService;
  private final ExecuteTransformation executeTransformation;
  private final FileExportRepository fileExportRepository;
  private final FileExportParamRepository fileExportParamRepository;
  private final JdbcTemplate jdbcTemplate;

  public FileExportResource(
    FileExportService fileExportService,
    ExecuteTransformation executeTransformation,
    FileExportParamService fileExportParamService,
    FileExportRepository fileExportRepository,
    FileExportParamRepository fileExportParamRepository,
    JdbcTemplate jdbcTemplate
  ) {
    this.fileExportService = fileExportService;
    this.executeTransformation = executeTransformation;
    this.fileExportParamService = fileExportParamService;
    this.fileExportRepository = fileExportRepository;
    this.fileExportParamRepository = fileExportParamRepository;
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * POST /file-exports : Create a new FileExport.
   *
   * @param FileExport the FileExport to create
   * @return the ResponseEntity with status 201 (Created) and with body the
   * new FileExport, or with status 400 (Bad Request) if the FileExport has
   * already an ID
   * @throws URISyntaxException if the Location URI syntax is incorrect
   */
  @PostMapping("/file-exports")
  @Timed
  public ResponseEntity<FileExport> createFileExport(
    @RequestBody FileExportDTO fileExportDTO
  )
    throws URISyntaxException, IOException {
    log.debug("REST request to save FileExport : {}", fileExportDTO);
    if (fileExportDTO.getFileExport().getId() != null) {
      throw new BadRequestAlertException(
        "A new FileExport cannot already have an ID",
        ENTITY_NAME,
        "idexists"
      );
    }
    fileExportDTO.getFileExport().setDateGeneration(LocalDate.now());
    FileExport result = fileExportService.save(fileExportDTO.getFileExport());

    for (FileExportParam param : fileExportDTO.getParams()) {
      param.setFileExport(result);
      FileExportParam f = fileExportParamService.save(param);
    }

//    executeTransformation.execTrans(
//      result.getExportTable().getId(),
//      result.getId()
//    );

    return ResponseEntity
      .created(new URI("/api/file-export/" + result.getId()))
      .headers(
        HeaderUtil.createEntityCreationAlert(
          ENTITY_NAME,
          result.getId().toString()
        )
      )
      .body(result);
  }

  /**
   * PUT /export-tables : Updates an existing FileExport.
   *
   * @param FileExport the FileExport to update
   * @return the ResponseEntity with status 200 (OK) and with body the updated
   * FileExport, or with status 400 (Bad Request) if the FileExport is not
   * valid, or with status 500 (Internal Server Error) if the FileExport
   * couldn't be updated
   * @throws URISyntaxException if the Location URI syntax is incorrect
   */
  @PutMapping("/file-exports")
  @Timed
  public ResponseEntity<FileExport> updateFileExport(
    @RequestBody FileExport fileExport
  )
    throws URISyntaxException {
    log.debug("REST request to update FileExport : {}", fileExport);
    if (fileExport.getId() == null) {
      //return createFileExport(fileExport);
    }
    FileExport result = fileExportService.save(fileExport);
    log.debug(
      "---------------------------------------{}",
      result.getExportTable().getId()
    );
    log.debug("---------------------------------------{}", result.getId());

    return ResponseEntity
      .ok()
      .headers(
        HeaderUtil.createEntityUpdateAlert(
          ENTITY_NAME,
          fileExport.getId().toString()
        )
      )
      .body(result);
  }

  /**
   * GET /file-exports : get all the FileExports.
   *
   * @param pageable the pagination information
   * @return the ResponseEntity with status 200 (OK) and the list of
   * FileExports in body
   */
  @GetMapping("/file-exports")
  @Timed
  public ResponseEntity<List<FileExport>> getAllFileExports(Pageable pageable) {
    log.debug("REST request to get a page of FileExports");
    Page<FileExport> page = fileExportService.findAll(pageable);
    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
      page,
      "/api/file-exports"
    );
    return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
  }
  
  @GetMapping("/file-exports-all")
  @Timed
  public List<FileExport> getAllFileExportsList() {
    log.debug("REST request to get a list of FileExports");
    List<FileExport> list = fileExportService.findAllList();
    
    return list;
  }

  /**
   * GET /file-exports/:id : get the "id" FileExport.
   *
   * @param id the id of the FileExport to retrieve
   * @return the ResponseEntity with status 200 (OK) and with body the
   * FileExport, or with status 404 (Not Found)
   */
  @GetMapping("/file-exports/{id}")
  @Timed
  public ResponseEntity<FileExport> getFileExport(@PathVariable Long id) {
    log.debug("REST request to get FileExport : {}", id);
    FileExport fileExport = fileExportService.findOne(id);
    return ResponseUtil.wrapOrNotFound(Optional.ofNullable(fileExport));
  }

  /**
   * DELETE /file-exports/:id : delete the "id" FileExport.
   *
   * @param id the id of the FileExport to delete
   * @return the ResponseEntity with status 200 (OK)
   */
  @DeleteMapping("/file-exports/{id}")
  @Timed
  public ResponseEntity<Void> deleteFileExport(@PathVariable Long id) {
    log.debug("REST request to delete FileExport : {}", id);
    List<FileExportParam> fileParams = fileExportParamRepository.findByExpotable(
      id
    );
    for (FileExportParam param : fileParams) {
      fileExportParamService.delete(param.getId());
    }
    fileExportService.delete(id);
    return ResponseEntity
      .ok()
      .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
      .build();
  }

  @GetMapping("/download/{id}")
  public ResponseEntity<Resource> downloadFile(@PathVariable Long id)
    throws IOException {
    FileExport fileExp = fileExportRepository.findById(id).orElse(null);
    executeTransformation.execTrans(
      fileExp.getExportTable().getId(),
      fileExp.getId()
    );

    String filePath = executeTransformation.getFile(id);
    String filename = filePath + ".csv";

    //chemin +"/"+ tenant + "/ouput/"+file.getFileName()+".txt";
    // String filename = chemin+"/"+"compte-client_20230303162154.txt";
    Resource resource = new UrlResource(Paths.get(filename).toUri());
    return ResponseEntity
      .ok()
      .header(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + fileExp.getFileName() + ".csv" + "\""
      )
      .contentType(MediaType.parseMediaType("text/plain"))
      .contentLength(resource.getFile().length())
      .body(resource);
  }

  @GetMapping("/file-export/getTableValue/{tableName}/{colonne}")
  @Timed
  public ResponseEntity<List<Map<String, Object>>> getColumnValue(
    @PathVariable String tableName,
    @PathVariable String colonne
  ) {
    log.debug("REST request to get param values : {}", tableName);
    String colonnes = "id";
    if (colonne.contains(";")) {
      String[] values = colonne.split(";");
      if (values != null && values.length > 1) {
        for (int i = 0; i < values.length; i++) {
          colonnes = colonnes + ',' + values[i];
        }
      }
    }
    log.debug("----------Colonnes----------{}", colonnes);
    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
      "SELECT distinct " + colonnes + " from " + tableName + " order by id asc"
    );
    log.debug(("-------------------------------------{}"), rows);

    return new ResponseEntity<>(rows, HttpStatus.OK);
  }
}
