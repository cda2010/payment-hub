package com.m2i.pti.web.rest;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Reclamation;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.EtatReclamation;
import com.m2i.pti.domain.enumeration.QualiteReclamation;
import com.m2i.pti.domain.TypeReclamation;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.mobile.dto.SettlementResponse;
import com.m2i.pti.repository.ClientRepository;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.PersonnePhysiqueRepository;
import com.m2i.pti.repository.PersonneRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.ReclamationService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.service.dto.PersonneDTO;
import com.m2i.pti.service.dto.PersonnePhysiqueDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;

import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.m2i.pti.domain.Reclamation}.
 */
@RestController
@RequestMapping("/api")
public class ReclamationResource {

    private static final String CLAIM_REASON_CORIS_MONEY = "coris_money";

    private final Logger log = LoggerFactory.getLogger(ReclamationResource.class);

    private static final String ENTITY_NAME = "reclamation";
    private final ReclamationService reclamationService;
    private final ApiService apiService;
    private final UniteOrganisationService uniteOrganisationService;
    private final PersonneRepository personneRepository;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;

    @Value("${tenant.numero}")
    private String tenantId;

    public ReclamationResource(ReclamationService reclamationService, ApiService apiService,
                               UniteOrganisationService uniteOrganisationService,
                               PersonneRepository personneRepository,
                               PersonnePhysiqueRepository personnePhysiqueRepository,
                               ClientRepository clientRepository,
                               CompteRepository compteRepository) {

        this.reclamationService = reclamationService;
        this.apiService = apiService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.personneRepository = personneRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.clientRepository = clientRepository;
        this.compteRepository = compteRepository;
    }

    /**
     * {@code POST  /reclamations} : Create a new reclamation.
     *
     * @param reclamation the reclamation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reclamation, or with status {@code 400 (Bad Request)} if the reclamation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/reclamations")
    public ResponseEntity<Reclamation> createReclamation(@RequestBody Reclamation reclamation) throws URISyntaxException {
        log.debug("REST request to save Reclamation : {}", reclamation);
        if (reclamation.getId() != null) {
            throw new BadRequestAlertException("A new reclamation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        reclamation.setDateHeureCreation(ZonedDateTime.now());
        reclamation.setEtat(EtatReclamation.EN_INSTANCE);
        Reclamation result = reclamationService.save(reclamation);
        /*List<MParametre> listparam = buildReclamationParametres(result);
        String codeFse = resolveCodeFse(result.getCodeActeur());
        try {
            SettlementResponse settlementResponse = this.apiService.settlement(listparam, codeFse);
            log.debug("External Reclamation settlement response : {}", settlementResponse);
        } catch (IOException | JAXBException e) {
            log.debug("Error request to save External Reclamation : {}", reclamation);
        }*/

        return ResponseEntity.created(new URI("/api/reclamations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /reclamations} : Updates an existing reclamation.
     *
     * @param reclamation the reclamation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reclamation,
     * or with status {@code 400 (Bad Request)} if the reclamation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reclamation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/reclamations")
    public ResponseEntity<Reclamation> updateReclamation(@RequestBody Reclamation reclamation) throws URISyntaxException {
        log.debug("REST request to update Reclamation : {}", reclamation);
        if (reclamation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        reclamation.setDateModification(ZonedDateTime.now());
        Reclamation result = reclamationService.save(reclamation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, reclamation.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /reclamations} : get all the reclamations.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reclamations in body.
     */
    @GetMapping("/reclamations")
    public ResponseEntity<List<Reclamation>>  getAllReclamations(@RequestParam(required = false)Long titre,
                                                @RequestParam(required = false) EtatReclamation etat,
                                                @RequestParam(required = false) QualiteReclamation qualiteReclamation,
                                                @RequestParam(required = false) String telephoneAuteur,
                                                @RequestParam(required = false) ZonedDateTime dateCreation,
                                                @RequestParam(required = false) ZonedDateTime dateModification,
                                                Pageable pageable) {
        log.debug("REST request to get all Reclamations");
        Page<Reclamation> page=reclamationService.findAll(titre,etat,qualiteReclamation,telephoneAuteur,dateCreation,dateModification,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/reclamations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    /**
     * {@code GET  /reclamations/:id} : get the "id" reclamation.
     *
     * @param id the id of the reclamation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reclamation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/reclamations/{id}")
    public ResponseEntity<Reclamation> getReclamation(@PathVariable Long id) {
        log.debug("REST request to get Reclamation : {}", id);
        Optional<Reclamation> reclamation = reclamationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reclamation);
    }

    /**
     * {@code DELETE  /reclamations/:id} : delete the "id" reclamation.
     *
     * @param id the id of the reclamation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/reclamations/{id}")
    public ResponseEntity<Void> deleteReclamation(@PathVariable Long id) {
        log.debug("REST request to delete Reclamation : {}", id);
        reclamationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    private List<MParametre> buildReclamationParametres(Reclamation reclamation) {
        List<MParametre> listparam = new ArrayList<>();
        listparam.add(new MParametre("CLAIMREASON", CLAIM_REASON_CORIS_MONEY, 1));
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            listparam.add(new MParametre("TENANTID", tenantId.trim(), 1));
        }
        if (reclamation.getId() != null) {
            listparam.add(new MParametre("REFERENCE", reclamation.getId().toString(), 1));
        }
        if (reclamation.getContenu() != null) {
            listparam.add(new MParametre("DESCRIPTION", reclamation.getContenu(), 1));
        }
        if (reclamation.getTelephone() != null) {
            listparam.add(new MParametre("PHONENUMBER", reclamation.getTelephone(), 1));
            listparam.add(new MParametre("CONCERNEDPHONENUMBER", reclamation.getTelephone(), 1));
        }
        PersonneDTO personne = findPersonne(reclamation);
        addClientIdentityParams(listparam, personne);
        String walletNumber = resolveWalletNumber(personne);
        if (walletNumber != null) {
            listparam.add(new MParametre("WALLETNUMBER", walletNumber, 1));
        }
        if (reclamation.getCodeActeur() != null) {
            listparam.add(new MParametre("CODEUO", reclamation.getCodeActeur(), 1));
        }
        if (reclamation.getCodeAuteur() != null) {
            listparam.add(new MParametre("CODEAUTEUR", reclamation.getCodeAuteur(), 1));
        }
        if (reclamation.getQualiteAuteur() != null) {
            listparam.add(new MParametre("QUALITEAUTEUR", reclamation.getQualiteAuteur().name(), 1));
        }
        if (reclamation.getEtat() != null) {
            listparam.add(new MParametre("ETAT", reclamation.getEtat().name(), 1));
        }
        if (reclamation.getAttribueA() != null) {
            listparam.add(new MParametre("ATTRIBUEA", reclamation.getAttribueA(), 1));
        }
        TypeReclamation typeReclamation = reclamation.getTypeReclamation();
        if (typeReclamation != null) {
            if (typeReclamation.getId() != null) {
                listparam.add(new MParametre("TYPE_RECLAMATION_ID", typeReclamation.getId().toString(), 1));
            }
            if (typeReclamation.getLibelleTypeReclamation() != null) {
                listparam.add(new MParametre("TYPE_RECLAMATION", typeReclamation.getLibelleTypeReclamation(), 1));
            }
        }
        return listparam;
    }

    private void addClientIdentityParams(List<MParametre> listparam, PersonneDTO personne) {
        if (personne == null) {
            return;
        }
        if (personne.getEmail() != null && !personne.getEmail().trim().isEmpty()) {
            listparam.add(new MParametre("EMAIL", personne.getEmail().trim(), 1));
        }
        if (personne.getPersonnePhysiqueId() != null) {
            PersonnePhysiqueDTO personnePhysique = personnePhysiqueRepository.findPersonneByIdClient(
                personne.getPersonnePhysiqueId());
            if (personnePhysique != null) {
                addPersonnePhysiqueParams(listparam, personnePhysique.getNomClient(),
                    personnePhysique.getPrenomClient());
            }
        }
    }

    private PersonneDTO findPersonne(Reclamation reclamation) {
        if (reclamation.getTelephone() == null || reclamation.getTelephone().trim().isEmpty()) {
            return null;
        }
        String codePaysTelephone = resolveCodePaysTelephone(reclamation.getCodeActeur());
        if (codePaysTelephone == null) {
            return null;
        }
        PersonneDTO personne = personneRepository.findPersonneByPhoneAndCodeIndicatifActivated(
            reclamation.getTelephone().trim(), codePaysTelephone);
        if (personne == null) {
            personne = personneRepository.findPersonneByPhoneAndCodeIndicatif(
                reclamation.getTelephone().trim(), codePaysTelephone);
        }
        return personne;
    }

    private String resolveWalletNumber(PersonneDTO personne) {
        if (personne == null || personne.getId() == null) {
            return null;
        }
        Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
        if (client == null) {
            return null;
        }
        List<CompteDTO> comptes = compteRepository.findByClientIdOptimise(client.getId());
        if (comptes == null) {
            return null;
        }
        for (CompteDTO compte : comptes) {
            if (compte.getCompteAutreId() != null && compte.getValeurCompte() != null
                && !compte.getValeurCompte().trim().isEmpty()) {
                return compte.getValeurCompte().trim();
            }
        }
        return null;
    }

    private void addPersonnePhysiqueParams(List<MParametre> listparam, String lastName, String firstName) {
        if (lastName != null && !lastName.trim().isEmpty()) {
            listparam.add(new MParametre("LASTNAME", lastName.trim(), 1));
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            listparam.add(new MParametre("FIRSTNAME", firstName.trim(), 1));
        }
    }

    private String resolveCodePaysTelephone(String codeUo) {
        if (codeUo == null || codeUo.trim().isEmpty()) {
            return null;
        }
        UniteOrganisation uo = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeUo.trim(), false);
        if (uo != null) {
            return uo.getCodePaysTelephone();
        }
        return null;
    }

    private String resolveCodeFse(String codeUo) {
        if (codeUo == null || codeUo.trim().isEmpty()) {
            return null;
        }
        UniteOrganisation uo = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeUo.trim(), false);
        if (uo != null) {
            return uo.getCodeFse();
        }
        log.debug("Unable to resolve codeFse for codeUO : {}", codeUo);
        return null;
    }
}
