package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.domain.CompteBanque;
import com.m2i.pti.domain.enumeration.CategorieCompte;
import com.m2i.pti.domain.enumeration.EtatCompte;
import com.m2i.pti.domain.enumeration.StatutCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.CompteAutreRepository;
import com.m2i.pti.repository.CompteBanqueRepository;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.ClientService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.EtiquetteCompteService;
import com.m2i.pti.service.TransactionComptePtiService;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.CompteAutreDTO;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.service.dto.PersonneAllDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Compte.
 */
@RestController
@RequestMapping("/api")
public class CompteResource {

    private final Logger log = LoggerFactory.getLogger(CompteResource.class);

    private static final String ENTITY_NAME = "compte";

    private final CompteService compteService;

    private final EtiquetteCompteService etiquetteCompteService;

    private final MobileService mobileService;

    private final TransactionComptePtiService transactionComptePtiService;
    public final UniteOrganisationRepository uniteOrganisationRepository;
    public final CompteRepository compteRepository;
    private final ClientService clientService;
    CompteAutreRepository compteAutreRepository;
    CompteBanqueRepository compteBanqueRepository;

    public CompteResource(UniteOrganisationRepository uniteOrganisationRepository, CompteBanqueRepository compteBanqueRepository, CompteAutreRepository compteAutreRepository, CompteService compteService, EtiquetteCompteService etiquetteCompteService, MobileService mobileService, TransactionComptePtiService transactionComptePtiService, CompteRepository compteRepository, ClientService clientService) {
        this.compteService = compteService;
        this.etiquetteCompteService = etiquetteCompteService;
        this.mobileService = mobileService;
        this.transactionComptePtiService = transactionComptePtiService;
        this.compteRepository = compteRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.compteBanqueRepository = compteBanqueRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.clientService = clientService;
    }

    /**
     * POST /comptes : Create a new compte.
     *
     * @param compteDTO the compteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new compteDTO, or with status 400 (Bad Request) if the compte has already
     * an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/comptes")
    @Timed
    @Secured({AuthoritiesConstants.CREATE_COMPTE_BANQUE})
    public ResponseEntity<Compte> createCompte(@Valid @RequestBody CompteDTO compteDTO) throws URISyntaxException {
        log.debug("REST request to save Compte : {}", compteDTO);
        if (compteDTO.getId() != null) {
            throw new BadRequestAlertException("A new compte cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CompteDTO result = compteService.saveNew(compteDTO);
        String malias = null;
        log.debug("entrer---" + result);
        if (result != null) {
            log.debug("entrer---" + result);
            log.debug("entrer---" + result);
            Compte compte = new Compte();

            compte.setClasseCompte(result.getClasseCompte());
            compte.categorieCompte(result.getCategorieCompte());
            if (result.getCompteBanqueDTO() != null) {

                compte.setStatutCompte(StatutCompte.DESACTIVE);
            } else {
                compte.setStatutCompte(StatutCompte.ACTIVE);
            }
            compte.setEtatCompte(EtatCompte.OUVERT);
            compte.setEtiquette(compteDTO.getEtiquette());
            compte.setLibelleCompte(compteDTO.getLibelleCompte());
            if (compteDTO.getComptePincipaleId() != null) {

                compte.setComptePincipale(compteService.findCompteOne(compteDTO.getComptePincipaleId()));
            }

            compte.setValeurCompte(compteDTO.getValeurCompte());
            compte.setFirstCategorieCompte(compteDTO.getFirstCategorieCompte());
            if (compteDTO.getUniteOrganisationId() != null) {
                compte.setUniteOrganisation(uniteOrganisationRepository.findById(compteDTO.getUniteOrganisationId()).orElse(null));
            }
            if (compteDTO.getClientId() != null) {
                //rechercher le cient
                Client cli = clientService.findClient(compteDTO.getClientId());
                        malias = compteService.getCRC32Checksum(result.getCompteBanqueDTO().getCodeAgence(), result.getValeurCompte(), cli.getPersonne().getTelephone(), result.getCompteBanqueDTO().getCodeFse());

                compte.setClient(cli);
            }
            if (result.getCompteBanqueDTO() != null) {
                CompteBanque cptBque = compteBanqueRepository.findById(result.getCompteBanqueDTO().getId()).orElse(null);
                cptBque.setAlias(malias);
                compte.setCompteBanque(cptBque);
            }
            if (result.getCompteAutreDTO() != null) {
                CompteAutre cptAutre = compteAutreRepository.findById(result.getCompteAutreDTO().getId()).orElse(null);
                compte.setCompteAutre(cptAutre);
            }

            Compte resul = compteRepository.save(compte);

            ///Compte   resul = compteService.saveCompte(result); 
            return ResponseEntity.created(new URI("/api/comptes/" + resul.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, resul.getId().toString()))
                    .body(compte);
        } else {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, null))
                    .body(null);
        }

    }

    /**
     * PUT /comptes : Updates an existing compte.
     *
     * @param compteDTO the compteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * compteDTO, or with status 400 (Bad Request) if the compteDTO is not
     * valid, or with status 500 (Internal Server Error) if the compteDTO
     * couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/comptes")
    @Timed
    @Secured({AuthoritiesConstants.CREATE_COMPTE_BANQUE})
    public ResponseEntity<CompteDTO> updateCompte(@Valid @RequestBody CompteDTO compteDTO) throws URISyntaxException {
        log.debug("REST request to update Compte : {}", compteDTO);
        if (compteDTO.getId() == null) {
            ResponseEntity<Compte> cpt = createCompte(compteDTO);
            Compte cptdto = cpt.getBody();
            compteDTO.setId(cptdto.getId());
            return ResponseEntity.created(new URI("/api/comptes/" + compteDTO.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, compteDTO.getId().toString()))
                    .body(compteDTO);

        }
        CompteDTO result = compteService.saveNew(compteDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, compteDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET /comptes : get all the comptes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of comptes
     * in body
     */
    @GetMapping("/comptes")
    @Timed
    public ResponseEntity<List<CompteDTO>> getAllComptes(Pageable pageable, @RequestParam(required = false) Long uoId, @RequestParam(required = false) String etiquette, @RequestParam(required = false) CategorieCompte categorieCompte, @RequestParam(required = false) String valeurCompte) {
        log.debug("REST request to get a page of Comptes");
        Page<CompteDTO> page = compteService.findAll1(pageable, uoId, etiquette, categorieCompte, valeurCompte);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/comptes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/comptes/titulaire")
    @Timed
    public ResponseEntity<List<PersonneAllDTO>> getAllComptes(@RequestParam String type, @RequestParam(required = false) String codeBanque, @RequestParam(required = false) String codeAgence, @RequestParam(required = false) String rib, @RequestParam(required = false) String valeurComtpe, Pageable pageable) {
        Page<PersonneAllDTO> page = compteService.findByCriteria(type, codeBanque, codeAgence, rib, valeurComtpe, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/personne");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    // @GetMapping("/comptes/{valeurCompte}/autres")
    @Timed
    public CompteAutre getCompteHasValeur(@PathVariable String valeurCompte) {
        return compteService.findByValeurCompte(valeurCompte);
        // return compteService.findCompteAutreByValeurCompte(valeurCompte);
    }

    @GetMapping("/comptes/{valeurCompte}/autres")
    @Timed
    public CompteAutreDTO getCompteByValeur(@PathVariable String valeurCompte) {
        // return compteService.findByValeurCompte(valeurCompte);
        return compteService.findCompteAutreByValeurCompte(valeurCompte);
    }

    @GetMapping("/returnComptePrincipalOfClient")
    @Timed
    public Compte getCompteOfClientByNum(@RequestParam(required = true) Long clientId, @RequestParam(required = false) String valeurCompte) {
        return compteService.getCptOfClient(clientId, valeurCompte);
    }

    @GetMapping("/countCoupteAutre")
    @Timed
    public HashMap getCountCptautre(@RequestParam(required = false) Long clientId, @RequestParam(required = false) String valeurCompte) {
        return compteService.getCountCptAutre(clientId, valeurCompte);
    }

    @GetMapping("/comptes/comptePrincipaleIsNull")
    @Timed
    public List<CompteDTO> getCompteWherePrincipaleIsNull(@RequestParam(required = false) Long uoId, @RequestParam(required = false) String etiquette) {
        log.debug("REST request to get a page of Comptes");
        List<CompteDTO> compteDTOList = compteService.findCompteAgenceSiegeAndOrMaster(uoId, etiquette);
        return compteDTOList;
    }

    /**
     * GET /comptes : get all the comptes.
     *
     * @param uoId information
     * @return the ResponseEntity with status 200 (OK) and the list of comptes
     * in body
     */
    @GetMapping("/comptesByUO/{uoId}")
    @Timed
    public List<CompteDTO> getComptesByUO(@PathVariable Long uoId) {
        log.debug("REST request to get a page of Comptes BY UO");
        List<CompteDTO> compteDTOList = compteService.findByUO(uoId);
        return compteDTOList;
    }

    /**
     * GET /valeurcomptes :
     *
     * @return the ResponseEntity with status 200 (OK) and the list of comptes
     * in body
     */
    @GetMapping("/valeurComptes")
    @Timed
    public CompteDTO getValeurComptes() {
        log.debug("REST request to get a valeur Compte");
        CompteDTO compteDTO = compteService.getValeurCompte();
        return compteDTO;
    }

    /**
     * GET /comptes/:id : get the "id" compte.
     *
     * @param id the id of the compteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * compteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/comptes/{id}")
    @Timed
    public ResponseEntity<CompteDTO> getCompte(@PathVariable Long id) {
        log.debug("REST request to get Compte : {}", id);
        CompteDTO compteDTO = compteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteDTO));
    }

    @GetMapping("/comptes/changeEtat/{id}")
    @Timed
    public ResponseEntity<CompteDTO> changeEtatCompte(@PathVariable Long id) {
        log.debug("REST request to get Compte : {}", id);
        CompteDTO compteDTO = compteService.changerEtatCompte(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteDTO));
    }

    @GetMapping("/comptes/changeStatut/{id}")
    @Timed
    public ResponseEntity<CompteDTO> changeStatutCompte(@PathVariable Long id) {
        log.debug("REST request to get Compte : {}", id);
        CompteDTO compteDTO = compteService.changerStatutCompte(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteDTO));
    }

    /*    @GetMapping("/comptes-banque/changeStatut/{id}")
    @Timed
    public ResponseEntity<CompteDTO> changeStatutCompteBanque(@PathVariable Long id){
        CompteDTO compteDTO=compteService.changerStatutCompteBanque(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteDTO));
    }*/
    @GetMapping("/compteById")
    @Timed
    public ResponseEntity<CompteDTO> getCompteById(String id) {
        log.debug("REST request to get Compte : {}", id);
        Long idN = Long.parseLong(id);
        CompteDTO compteDTO = compteService.findOne(idN);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteDTO));
    }

    /**
     * DELETE /comptes/:id : delete the "id" compte.
     *
     * @param id the id of the compteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        log.debug("REST request to delete Compte : {}", id);
        compteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/comptes/solde_compte_pti")
    @Timed
    public BigDecimal getSoldeCompteInterne(@RequestParam String numCompte) {
        return compteService.getSolde(numCompte);
    }

    @GetMapping("/comptes/ecriture")
    @Timed
    public void instructionComptable(@RequestParam String numSrc, @RequestParam String numDest, @RequestParam BigDecimal montant) {
        transactionComptePtiService.ecritureCompte(numSrc, numDest, montant);
    }

    @GetMapping("/findByCptAutre")
    @Timed
    public Compte findCptByCptAutre(@RequestParam Long compteAutreId) {
        return compteRepository.findByCompteAutreId(compteAutreId);
    }

    @GetMapping("/findCompteByValeurCompte")
    @Timed
    public Compte findCptByValCpt(@RequestParam String valeurCompte, @RequestParam String codePays) {
        return mobileService.findComptePersonne(valeurCompte, codePays);
    }

    @GetMapping("/getValeurCompteOfClient")
    @Timed
    public String getValeurCompteOfClient(@RequestParam Long codeFse) {
        return compteService.getNumeroCompteOfClient(codeFse);
    }

    @GetMapping("/comptes-by-client-tel/{numeroTel}")
    @Timed
    public List<Compte> getComptesByUO(@PathVariable String numeroTel) {
        log.debug("REST request to get a page of Comptes BY UO");
        List<Compte> compteDTOList = compteService.findByNumeroTel(numeroTel);
        return compteDTOList;
    }

    @GetMapping("/add-compte-organisation")
    @Timed
    public  void  saveCompteOrganisation(@RequestParam Long organisationId){
         compteService.saveCompteOfOrganisation(organisationId);
    }


}
