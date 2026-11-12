package com.m2i.pti.repository;

import com.m2i.pti.domain.TemplateMessage;
import com.m2i.pti.domain.enumeration.Destination;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the TemplateMessage entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TemplateMessageRepository extends JpaRepository<TemplateMessage, Long> {
@Query("select t from  TemplateMessage t where t.typeServiceLibelle=?1 and t.destination=?2")
    public TemplateMessage findByTypeService(TypeServiceLibelle typeservice, Destination dest);
    
    @Query("select t from  TemplateMessage t where t.typeServiceLibelle=?1 and t.destination=?2")
    public  List<TemplateMessage>  findByTypeServiceList(TypeServiceLibelle typeservice, Destination dest);
 @Query("select t from  TemplateMessage t where t.etiquette=?1 and t.destination=?2")
    public TemplateMessage findByEtiquette(String etiquette, Destination dest);

 @Query("select t from TemplateMessage t where (:typeServiceLibelle is null or t.typeServiceLibelle=:typeServiceLibelle)")
    Page<TemplateMessage> findAllByTypeServiceLibelle(Pageable pageable, @Param("typeServiceLibelle") TypeServiceLibelle typeServiceLibelle);
}
