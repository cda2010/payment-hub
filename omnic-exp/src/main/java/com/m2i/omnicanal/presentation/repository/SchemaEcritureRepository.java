package com.m2i.omnicanal.presentation.repository;



import com.m2i.omnicanal.model.SchemaEcriture;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.presentation.domain.dto.SchemaDto;
import com.m2i.omnicanal.presentation.domain.SchemaEcrit;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the SchemaEcriture entity.
 */
@SuppressWarnings("unused")
public interface SchemaEcritureRepository extends EntityRepository<SchemaEcrit,Long> {
 @Query("select s from  SchemaEcrit   s where s.typeService=?1 order by s.id")
    public List<SchemaEcriture> findByService(TypeService typeService);
    
    
    
    @Query(value="select schemaecri0_.id as id2_22_, schemaecri0_.code_groovy as code_gro3_22_, schemaecri0_.nom_schema as nom_sche4_22_, \n" +
"schemaecri0_.type_service_id as type_ser5_22_ from schema_ecriture schemaecri0_ where \n" +
"schemaecri0_.extype='SchemaEcrit' and schemaecri0_.type_service_id=?1 order by schemaecri0_.id", nativeQuery=true)
    public List<SchemaDto> findByService(Long typeServiceId);

}
