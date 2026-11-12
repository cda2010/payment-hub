package com.m2i.pti.repository;

import com.m2i.pti.domain.Terminal;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Terminal entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
@Query("select t from Terminal t where t.uuid=?1")
    public Terminal findByUiid(String uuid);

}
