package com.m2i.pti.repository;

import com.m2i.pti.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * Spring Data JPA repository for the Document entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTitulaireId(Long titulaireId);

    @Query("select d from Document d where d.imageFolder=?1")
    Document findByImageFolder(String folder);

    @Query("select d from Document d where d.typeDocument.id=?1 and d.titulaire.id=?2")
    Document findByTypeDocumentAndTitulaireId(Long typeDocument,Long titulaireId);
    
    @Query(value="select * from document d where d.image_folder is null order by d.id desc limit :fin",nativeQuery = true)
    List<Document> findAllDocWithoutImageDirLimit(@Param("fin") Long fin);

}
