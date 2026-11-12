package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.batch.reader.*;
import com.m2i.pti.repository.ChargementTransfertRepository;
import org.springframework.batch.item.ItemReader;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * This class demonstrates how we can implement a custom ItemReader.
 */
public class InMemoryLigneChargementTransfertReader implements ItemReader< LigneChargementTransDTO> {

    @Autowired
    private ChargementTransfertRepository chargementTransfertRepository;

    private int nextLigneIndex = 0;
    private List<LigneChargementTransDTO> listeligneChargementTransDTO;
    @Value("#{jobParameters['codeoperation']}")
    Long codeoperation;
    @Value("#{jobParameters['compteId']}")
    Long compteId;
    @Value("#{jobParameters['chargementId']}")
    Long idChargement;

    public InMemoryLigneChargementTransfertReader() {
        initialize();

    }

    private void initialize() {
        nextLigneIndex = 0;
    }

//    private List<LigneChargementTransDTO> findByChargementId(Long codeoperation,
//            Long compteId,
//            Long idChargement) {
//        String sql = "SELECT id as idLigne, " + codeoperation + " as codeoperation ," + compteId + " as compteId FROM ligne_chargement_transfert  where chargement_transfert_id= " + idChargement + " and statut_ligne!='OC' and compte_id is not null order by id";
//        System.out.println(sql);
//        nextLigneIndex = 0;
//        List<LigneChargementTransDTO> listeLig= jdbcTemplate.query(
//                sql,
//                new BeanPropertyRowMapper(LigneChargementTransDTO.class));
//        return listeLig;
//
//    }
    @Override
    public LigneChargementTransDTO read() throws Exception {
        listeligneChargementTransDTO = chargementTransfertRepository.findByChargementId(codeoperation, compteId, idChargement);
        System.out.println("NOMBRE DE LIGNE" + listeligneChargementTransDTO.size());
        LigneChargementTransDTO nextLigneChargement = null;
        if (nextLigneIndex < listeligneChargementTransDTO.size()) {
            nextLigneChargement = listeligneChargementTransDTO.get(nextLigneIndex);
            nextLigneIndex++;
        } else {
            nextLigneIndex = 0;
        }

        return nextLigneChargement;
    }
}
