/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author USER_M2I
 */
public class LigneChargementRowMapper implements RowMapper<LigneChargementTransDTO> {

    @Override
    public LigneChargementTransDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        LigneChargementTransDTO ligneChargement = new LigneChargementTransDTO();
            ligneChargement.setIdOperation(rs.getLong("codeoperation"));
            ligneChargement.setCompteId(rs.getLong("compteId"));
            ligneChargement.setIdLigneChargement(rs.getLong("idLigne"));
            return ligneChargement;
    }

}
