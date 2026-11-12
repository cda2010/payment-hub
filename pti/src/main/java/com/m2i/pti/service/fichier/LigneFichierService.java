package com.m2i.pti.service.fichier;

import com.m2i.pti.domain.LigneChargementFichier;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.repository.LigneChargementFichierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LigneFichierService {

    private final LigneChargementFichierRepository ligneChargementFichierRepository;

    public LigneFichierService(LigneChargementFichierRepository ligneChargementFichierRepository) {
        this.ligneChargementFichierRepository = ligneChargementFichierRepository;
    }

    public LigneChargementFichier save(LigneChargementFichier ligneChargementFichier){
        return ligneChargementFichierRepository.save(ligneChargementFichier);
    }

    public Page<LigneChargementFichier> queryDetailPageByStatut(Long id, StatutLigneChargement statutLigne, Pageable pageable) {

            return ligneChargementFichierRepository.queryDetailPageByStatut(id,statutLigne,pageable);
    }
    public Page<LigneChargementFichier> queryDetailPageByOrganisation(Long id,Pageable pageable) {

        return ligneChargementFichierRepository.queryDetailPageByOrganisation(id,pageable);
    }

    public Page<LigneChargementFichier> queryDetailPageByStatut1(Long id, StatutLigneChargement statutLigne, StatutLigneChargement statutLigne1, Pageable pageable) {
        return ligneChargementFichierRepository.queryDetailPageByStatut1(id,statutLigne,statutLigne1,pageable);
    }

    public List<LigneChargementFichier> findByChargementFichierIdAndStatutLigneChargement(Long id, StatutLigneChargement ic) {
        return ligneChargementFichierRepository.findByChargementFichierIdAndStatutLigneChargement(id,ic);
    }

    public LigneChargementFichier findOne(Long id){
        return ligneChargementFichierRepository.findById(id).orElse(null);
    }
}
