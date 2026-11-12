package com.m2i.pti.service.minio;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Document;
import com.m2i.pti.domain.TypeDocument;
import com.m2i.pti.repository.ClientRepository;
import com.m2i.pti.repository.DocumentRepository;
import com.m2i.pti.repository.PersonneRepository;
import com.m2i.pti.repository.TypeDocumentRepository;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.DocumentDTO;
import com.m2i.pti.service.dto.MinioFolderDTO;
import com.m2i.pti.service.dto.PersonneDTO;
import com.m2i.pti.service.mapper.ClientMapper;
import com.m2i.pti.service.mapper.DocumentMapper;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.print.Doc;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.folder}")
    private String repertoireFiliale;
    private static final FileNameMap typeFile = URLConnection.getFileNameMap();
    private final PersonneRepository personneRepository;
    private final ClientRepository clientRepository;
    private final DocumentRepository documentRepository;
    private final ClientMapper clientMapper;
    private final TypeDocumentRepository typeDocumentRepository;
    private final DocumentMapper documentMapper;

    public MinioService(PersonneRepository personneRepository, ClientRepository clientRepository, DocumentRepository documentRepository, ClientMapper clientMapper, TypeDocumentRepository typeDocumentRepository, DocumentMapper documentMapper) {
        this.personneRepository = personneRepository;
        this.clientRepository = clientRepository;
        this.documentRepository = documentRepository;
        this.clientMapper = clientMapper;
        this.typeDocumentRepository = typeDocumentRepository;
        this.documentMapper = documentMapper;
    }

    public MinioFolderDTO loadDataFromPenthaoAndGetFolder(String cheminFichier, String dossier, String shortFolderName) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String imageName;
        String folder = "";
        String contentType;
        Long typeDocument;
        shortFolderName = shortFolderName.substring(3);
        MinioFolderDTO minioFolderDTO = new MinioFolderDTO();
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            } else {
                System.out.println("Bucket " + bucket + " already exists.");
            }
            File file = new File(cheminFichier);
            System.out.println("---------Is File-" + file.isFile());
            Boolean check = (SystemUtils.IS_OS_LINUX);
            if (file.isFile() || check) {
                System.out.println("--------------------------------");
                System.out.println("--------------------------------");
                System.out.println("file name ++" + file.getName());
                System.out.println("file-----------" + file.getAbsolutePath());
                System.out.println("--------------------------------");
                System.out.println("----------------FIN----------------");
                contentType = typeFile.getContentTypeFor(file.getName());
                minioFolderDTO.setContentType(contentType);
                if (file.getName().equals("PORTRAIT.jpg")) {
                    typeDocument = 2L;
                    minioFolderDTO.setTypeDocument(typeDocument);
                    imageName = "PHOTO_IDENTITE";
                    folder = dossier + "/" + shortFolderName + "/" + imageName + ".jpg";
                    minioFolderDTO.setFolder(folder);
                    System.out.println("name ++" + imageName);
                    minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(dossier + "/" + shortFolderName + "/" + imageName + ".jpg")
                                    .filename(file.getAbsolutePath())
                                    .build());

                } else if (file.getName().equals("PROOF_OF_IDENTITY.jpg")) {
                    typeDocument = 1L;
                    minioFolderDTO.setTypeDocument(typeDocument);
                    imageName = "IDENTITE_FACE";
                    System.out.println("name ++" + imageName);
                    folder = dossier + "/" + shortFolderName + "/" + imageName + ".jpg";
                    minioFolderDTO.setFolder(folder);
                    minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(dossier + "/" + shortFolderName + "/" + imageName + ".jpg")
                                    .filename(file.getAbsolutePath())
                                    .build());

                } else if (file.getName().equals("SUBSCRIPTION_FORM.jpg")) {

                    typeDocument = 4L;
                    minioFolderDTO.setTypeDocument(typeDocument);
                    imageName = "FICHE_SOUSCRIPTION";
                    folder = dossier + "/" + shortFolderName + "/" + imageName + ".jpg";
                    minioFolderDTO.setFolder(folder);
                    System.out.println("name ++" + imageName);
                    minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(dossier + "/" + shortFolderName + "/" + imageName + ".jpg")
                                    .filename(file.getAbsolutePath())
                                    .build());

                } else {
                    typeDocument = 3L;
                    minioFolderDTO.setTypeDocument(typeDocument);
                    imageName = "IDENTITE_DOS";
                    folder = dossier + "/" + shortFolderName + "/" + imageName + ".jpg";
                    minioFolderDTO.setFolder(folder);
                    System.out.println("name ++" + imageName);
                    minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(dossier + "/" + shortFolderName + "/" + imageName + ".jpg")
                                    .filename(file.getAbsolutePath())
                                    .build());

                }
                return minioFolderDTO;
            }

        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }

        return minioFolderDTO;
    }

    public String getImageFromMinio(String imageFolder) throws Exception  {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        String urlFileLink = "";
        if (!found) {
            // Make a new bucket called 'asiatrip'.
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        } else {
            System.out.println("Bucket " + bucket + " already exists.");
        }
        System.out.println("********************");
        System.out.println("**********imageFolder**********" + imageFolder);
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

    public String uploadPubliciteImage(MultipartFile file, String pays) throws Exception {
        String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();  // Création d'un nom unique
        String filePath = pays + "/" + fileName;  // Dossier par pays
        String bucketPub = "publicites";

        // Créer un client MinIO
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            // Verifier si le bucket existe
            boolean found = minioClient.bucketExists(bucketPub);
            if (!found) {
                minioClient.makeBucket(bucketPub);  // Créer le bucket
            }

            // Uploader le fichier dans MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketPub)
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Return l'URL de l'image sur MinIO
            return minioClient.getObjectUrl(bucketPub, filePath);

        } catch (MinioException | IOException e) {
            throw new Exception("Erreur lors de l'upload de l'image vers MinIO: " + e.getMessage());
        }
    }

    public MinioFolderDTO loadDataFromPtiAndGetFolder(DocumentDTO documentDTO, String telephone) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String typeFile = ".png";
        String typeDocument = "";
        String repFiliale = repertoireFiliale;
        MinioFolderDTO minioFolderDTO = new MinioFolderDTO();
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            } else {
                System.out.println("Bucket " + bucket + " already exists.");
            }

            // TODO: 08/05/2024 Enregistrement sur minio de l'image
            if (documentDTO.getValeurContentType().equals("image/jpg")) {
                typeFile = ".jpg";
            } else if (documentDTO.getValeurContentType().equals("image/jpeg")) {
                typeFile = ".jpeg";
            } else {
                typeFile = ".png";
            }
            typeDocument = documentDTO.getTypeDocumentCode();
            typeDocument = typeDocument.replace(" ", "_");
            String fileName = typeDocument + typeFile;
            String folder = repFiliale + "/" + telephone + "/" + fileName;
            System.out.println("Object name " + folder);
            System.out.println("content name " + documentDTO.getValeur());
            System.out.println("typeDocument name " + typeDocument);
            minioFolderDTO.setFolder(folder);
            minioFolderDTO.setContentType(documentDTO.getValeurContentType());
            minioFolderDTO.setTypeDocument(documentDTO.getTypeDocumentId());
            InputStream inputStream = new ByteArrayInputStream(documentDTO.getValeur());

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(folder)
                            .stream(inputStream, documentDTO.getValeur().length, -1) // spécifiez la longueur du flux et -1 pour une longueur inconnue
                            .build());

        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }

        return minioFolderDTO;
    }

    public MinioFolderDTO updateDataFromPtiAndGetFolder(DocumentDTO documentDTO, String telephone) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String repFiliale = repertoireFiliale;
        String typeFile = ".png";
        String typeDocument = "";
        MinioFolderDTO minioFolderDTO = new MinioFolderDTO();
        if (documentDTO.getValeur() != null) {
            try {
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials(accessKey, secretKey)
                        .build();
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!found) {
                    // Make a new bucket called 'asiatrip'.
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                } else {
                    System.out.println("Bucket " + bucket + " already exists.");
                }
                if (documentDTO.getValeurContentType().equals("image/jpg")) {
                    typeFile = ".jpg";
                } else if (documentDTO.getValeurContentType().equals("image/jpeg")) {
                    typeFile = ".jpeg";
                } else {
                    typeFile = ".png";
                }
                typeDocument = documentDTO.getTypeDocumentCode();
                typeDocument = typeDocument.replace(" ", "_");
                String fileName = typeDocument + typeFile;
                String folder = repFiliale + "/" + telephone + "/" + fileName;
                minioFolderDTO.setFolder(folder);
                minioFolderDTO.setContentType(documentDTO.getValeurContentType());
                minioFolderDTO.setTypeDocument(documentDTO.getTypeDocumentId());
                InputStream inputStream = new ByteArrayInputStream(documentDTO.getValeur());
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(folder)
                                .stream(inputStream, documentDTO.getValeur().length, -1) // spécifiez la longueur du flux et -1 pour une longueur inconnue
                                .build());
            } catch (MinioException e) {
                System.out.println("Error occurred: " + e);
            }
        } else {
            minioFolderDTO.setTypeDocument(documentDTO.getTypeDocumentId());
            minioFolderDTO.setFolder(documentDTO.getImageFolder());
            minioFolderDTO.setContentType(documentDTO.getValeurContentType());
        }

        return minioFolderDTO;
    }

    public Document saveDocumentClient(String cheminFichier, String dossier, String shortFolderName) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        MinioFolderDTO folder = loadDataFromPenthaoAndGetFolder(cheminFichier, dossier, shortFolderName);
        Document document = new Document();
        TypeDocument typeDocument = null;
        String telephone = shortFolderName.substring(3);
        System.out.println("---------------------telephone-----------" + telephone);
        PersonneDTO personneDTO = personneRepository.findPersonneByPhoneAndCodeIndicatif(telephone, "226");
        if (personneDTO != null && folder != null) {
            System.out.println("---------------------personneDTO-----------" + personneDTO.toString());
            ClientDTO clientDTO = clientRepository.findClientPersonne(personneDTO.getId());
            document.setTitulaire(clientMapper.toEntity(clientDTO));
            if (folder.getTypeDocument() != null) {
                typeDocument = typeDocumentRepository.findById(folder.getTypeDocument()).orElse(null);
            }
            document.setTypeDocument(typeDocument);
            document.setValeurContentType(folder.getContentType());
            document.setImageFolder(folder.getFolder());
            System.out.println("---------------------document-----------" + document.getImageFolder());
            Document checkDocExiste = documentRepository.findByImageFolder(folder.getFolder());
            if (checkDocExiste != null && checkDocExiste.getId() != null) {
                System.out.println("------Document Existe------");
            } else {
                if (typeDocument != null && typeDocument.getId() != null) {
                    Document docExiste = documentRepository.findByTypeDocumentAndTitulaireId(typeDocument.getId(), clientDTO.getId());
                    if (docExiste != null && docExiste.getId() != null) {
                        System.out.println("------Document Existant dans base------");

                    } else {
                        documentRepository.save(document);
                    }
                }
            }
            return document;
        }
        return null;
    }

    public Document saveDocumentCLientInFolder(DocumentDTO document, Client client) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String telephone = client.getPersonne().getTelephone();
        Document documentNew = new Document();
        System.out.println("type doc" + document.getTypeDocumentCode());
        System.out.println("libelle doc" + document.getTypeDocumentLibelle());
        MinioFolderDTO folder = loadDataFromPtiAndGetFolder(document, telephone);
        if (folder != null && telephone != null) {
            documentNew.setTitulaire(client);
            documentNew.setTypeDocument(typeDocumentRepository.findById(folder.getTypeDocument()).orElse(null));
            documentNew.setValeurContentType(folder.getContentType());
            documentNew.setImageFolder(folder.getFolder());
            System.out.println("---------------------document-----------" + documentNew.getImageFolder());
            Document checkDocExiste = documentRepository.findByImageFolder(folder.getFolder());
            if (checkDocExiste != null && checkDocExiste.getId() != null) {
                System.out.println("------Document Existe------");
            } else {
                documentRepository.save(documentNew);
                System.out.println("saved doc-------");
            }
            return documentNew;
        }
        return null;
    }

    public Document updateDocumentCLientInFolder(DocumentDTO doc, Client client) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String telephone = client.getPersonne().getTelephone();
        MinioFolderDTO minioFolderDTO = updateDataFromPtiAndGetFolder(doc, telephone);
        Document document = documentRepository.findById(doc.getId()).orElse(null);
        document.setImageFolder(minioFolderDTO.getFolder());
        TypeDocument typeDocument = typeDocumentRepository.findById(minioFolderDTO.getTypeDocument()).orElse(null);
        document.setTypeDocument(typeDocument);
        document.setValeurContentType(minioFolderDTO.getContentType());
        document.setValeur(null);
        documentRepository.save(document);
        return document;
    }

    public void migrateImageFromPti(Long fin) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("Entere----");
        List<Document> listDoc = documentRepository.findAllDocWithoutImageDirLimit(fin);
        System.out.println("Entere----" + listDoc.size());
        for (Document doc : listDoc) {
            DocumentDTO documentDTO = documentMapper.toDto(doc);
            System.out.println("DEBUT FONCTION----" + documentDTO);
            if (doc.getValeur() != null) {
                MinioFolderDTO minioFolderDTO = updateDataFromPtiAndGetFolder(documentDTO, doc.getTitulaire().getPersonne().getTelephone());
                System.out.println("FIN FONCTION----" + minioFolderDTO.toString());
                if (minioFolderDTO.getFolder() != null) {
                    System.out.println("DEBUT DE LA MISE A JOURS----");
                    doc.setImageFolder(minioFolderDTO.getFolder());
                    documentRepository.save(doc);
                    System.out.println("FIN DE LA MISE A JOURS----");
                }
            }
            System.out.println("IMAGE INNEXISTANT----" + doc.getId());
        }
        System.out.println("Erreur de sortie----" + listDoc.size());
    }

}
