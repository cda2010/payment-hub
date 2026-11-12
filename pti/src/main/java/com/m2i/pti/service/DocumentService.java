package com.m2i.pti.service;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Document;
import com.m2i.pti.repository.DocumentRepository;
import com.m2i.pti.service.dto.DocumentDTO;
import com.m2i.pti.service.mapper.DocumentMapper;
import com.m2i.pti.service.minio.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;


/**
 * Service Implementation for managing Document.
 */
@Service
@Transactional
public class DocumentService {

    private final Logger log = LoggerFactory.getLogger(DocumentService.class);

    @Value("${m2i.security.pti.images.windows}")
    private String imagesPathWindows;

    @Value("${m2i.security.pti.images.linux}")
    private String imagesPathLinux;

    @Value("${minio.endpoint}")
    private String endpoint;
    
    @Value("${minio.getURL}")
    private String getURL;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    private final DocumentRepository documentRepository;

    private final MinioService minioService;

    private final DocumentMapper documentMapper;

    public DocumentService(DocumentRepository documentRepository, MinioService minioService, DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.minioService = minioService;
        this.documentMapper = documentMapper;
    }

    /**
     * Save a document.
     *
     * @param documentDTO the entity to save
     * @return the persisted entity
     */
    public DocumentDTO save(DocumentDTO documentDTO) {
        log.debug("Request to save Document : {}", documentDTO);
        Document document = documentMapper.toEntity(documentDTO);
        document = documentRepository.save(document);
        return documentMapper.toDto(document);
    }

    /**
     *  Get all the documents.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DocumentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Documents");
        return documentRepository.findAll(pageable)
            .map(documentMapper::toDto);
    }

    /**
     *  Get one document by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public DocumentDTO findOne(Long id) {
        log.debug("Request to get Document : {}", id);
        Document document = documentRepository.findById(id).orElse(null);
        return documentMapper.toDto(document);
    }

    /**
     *  Delete the  document by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Document : {}", id);
        documentRepository.deleteById(id);
    }

    public void saveImageClientToFolder(DocumentDTO documentDTO,String telephone,Long titulaireId,Boolean backOffice) throws IOException {
        String folder=null;
        String imageName=null;
        String typeImage;
        byte[] content= documentDTO.getValeur();
        String contentType=documentDTO.getValeurContentType();
        imageName=documentDTO.getTypeDocumentCode();
        imageName=imageName.replace(" ", "_");
        if(backOffice){
             typeImage=TransformToPngBackOffice(contentType);
        }else {
             typeImage=TransformToPng(contentType);
        }

        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? imagesPathLinux : imagesPathWindows);
        File imageDirectory = new File(keyDirectory);
        log.debug("folder-----"+imageDirectory.getParent()+"and "+imageDirectory.getAbsolutePath());

        File imageFolder=new File (imageDirectory.getAbsolutePath(),telephone);
        if (!imageFolder.exists()) {
            imageFolder.mkdir();
        }

        File clientFolder= new File(imageFolder.getAbsolutePath(),titulaireId.toString());
        if (!clientFolder.exists()) {
            clientFolder.mkdir();
        }
       /* clientFolder.setReadable(true);
        clientFolder.setWritable(true);
        clientFolder.setExecutable(true);*/

        log.debug("+++++++11**"+clientFolder);
        log.debug("xontent type*************---"+contentType);
        log.debug("****typeImage*****"+typeImage);
        try {
            log.debug("+++++++22**"+clientFolder);
            ByteArrayInputStream bis = new ByteArrayInputStream(content);
            log.debug("****---------V1---------------*****");
            Iterator<?> readers = ImageIO.getImageReadersByFormatName(typeImage);
            ImageReader reader = (ImageReader) readers.next();
            Object source = bis;
            log.debug("****---------V-2--------------*****");
            ImageInputStream iis = ImageIO.createImageInputStream(source);
            log.debug("****---------V-2.1--------------*****");
            reader.setInput(iis,true);
            log.debug("****---------V-2-.2-------------*****");
            ImageReadParam param = reader.getDefaultReadParam();
            log.debug("****---------V-2-.3-------------*****");
            Image image = reader.read(0, param);
            log.debug("****---------V-3--------------*****");
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.drawImage(image, null, null);
            log.debug("****---------Generate file---------------*****");
            File imageFile =  new File(clientFolder+"/"+imageName+"."+typeImage);
            log.debug("fichier image **********"+imageFile.createNewFile());
            imageFile.createNewFile();
            log.debug("****---------Fin file---------------*****");
            ImageIO.write(bufferedImage, "png", imageFile);
            log.debug("****---------Fin image---------------*****");

        } catch (Exception e){
            log.debug("impossible de generer le fichier", e.getMessage());
            log.debug("Message error"+e.getLocalizedMessage());
            log.debug("**************************",e.getCause());
            log.debug("****--------------*********"+e.toString());

        }

    }



    public String TransformToPng(String contentType){
        String  ft ="png";
        if(contentType.equals("image/png")){
            ft= "png";
        } else if(contentType.equals("image/jpeg")){
            ft= "png";
        } else if(contentType.equals("image/jpg")){
            ft= "png";
        }
        else {
            ft="png";
        }
        return ft;
    }

    public String TransformToPngBackOffice(String contentType){
        String  ft ="png";
        if(contentType.equals("image/png")){
            ft= "png";
        } else if(contentType.equals("image/jpeg")){
            ft= "jpeg";
        } else if(contentType.equals("image/jpg")){
            ft= "jpg";
        }
        else {
            ft="png";
        }
        return ft;
    }


    public String getImageFromMinio(String imageFolder) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, InvalidBucketNameException, ErrorResponseException, RegionConflictException, InvalidExpiresRangeException {
        System.out.println("getURL"+getURL);
        MinioClient minioClient = MinioClient.builder()
                .endpoint(getURL)
                .credentials(accessKey, secretKey)
                .build();
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        String urlFileLink="";
        if (!found) {
            // Make a new bucket called 'asiatrip'.
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        } else {
            System.out.println("Bucket " + bucket + " already exists.");
        }
        System.out.println("********************");
        System.out.println("**********imageFolder**********"+imageFolder);
        urlFileLink = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(imageFolder)
                        .expiry(60 * 60)
                        .build()
        );

        System.out.println("********************");
        System.out.println("URL de l'objet pré-signée : " + urlFileLink);
        return urlFileLink;
    }
    public Document saveDocInMinioFromMobile (DocumentDTO document, Client client) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        Document doc=minioService.saveDocumentCLientInFolder(document,client);
        return doc;
    }
}
