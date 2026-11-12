package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteBanque;
import com.m2i.pti.domain.Reclamation;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.ClientService;
import com.m2i.pti.service.dto.ClientAllDTO;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.CompteBanqueDTO;
import com.m2i.pti.service.dto.XCompteBanqueDTO;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Client.
 */
@RestController
@RequestMapping("/api")
public class ClientResource {

    private final Logger log = LoggerFactory.getLogger(ClientResource.class);

    private static final String ENTITY_NAME = "client";

    private final ClientService clientService;


    public ClientResource(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * POST  /clients : Create a new client.
     *
     * @param clientDTO the clientDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new clientDTO, or with status 400 (Bad Request) if the client has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
  /*  @PostMapping("/clients")
    @Timed
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO) throws URISyntaxException {
        log.debug("REST request to save Client : {}", clientDTO);
        if (clientDTO.getId() != null) {
            throw new BadRequestAlertException("A new client cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ClientDTO result = clientService.save(clientDTO);
        return ResponseEntity.created(new URI("/api/clients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }*/
    @PostMapping("/clients")
    @Timed
    public ResponseEntity<ClientDTO> createClientWithAll(@RequestBody ClientAllDTO clientDTO) throws URISyntaxException {

        ClientDTO result = clientService.saveInfosClient(clientDTO);
        return ResponseEntity.created(new URI("/api/clients/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /clients : Updates an existing client.
     *
     * @param clientDTO the clientDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated clientDTO,
     * or with status 400 (Bad Request) if the clientDTO is not valid,
     * or with status 500 (Internal Server Error) if the clientDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
  /*  @PutMapping("/clients")
    @Timed
    public ResponseEntity<ClientDTO> updateClient(@RequestBody ClientDTO clientDTO) throws URISyntaxException {
        log.debug("REST request to update Client : {}", clientDTO);
        if (clientDTO.getId() == null) {
            return createClient(clientDTO);
        }
        ClientDTO result = clientService.save(clientDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, clientDTO.getId().toString()))
            .body(result);
    }*/

    /**
     * GET  /clients : get all the clients.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of clients in body
     */
    @GetMapping("/clients")
    @Timed
    public List<ClientAllDTO> getAllClients(@RequestParam(required = false) String code, @RequestParam(required = false) String telephone, Pageable pageable) {
        log.debug("REST request to get a page of Clients");
        return clientService.findAll(pageable, telephone, code);
    }
    @GetMapping("/clients/type")
    @Timed
    public ResponseEntity<List<ClientAllDTO>> findByTypePersonne(@RequestParam(required = true) TypePersonne typePersonne,@RequestParam(required = false) String code, @RequestParam(required = false) String telephone, @RequestParam(required = false) String adresse,@RequestParam(required = false)String nom,@RequestParam(required = false)String prenom,@RequestParam(required = false) String raisonSociale,@RequestParam(required = false) String numeroPiece, Pageable pageable) {
        log.debug("REST request to get a page of Clients");
        Page<Client> page=clientService.findByTypePersonnePage(typePersonne,code,telephone,adresse,nom,prenom,raisonSociale,numeroPiece,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/clients/type");
        List<ClientAllDTO> p=clientService.getAllDTO(page,numeroPiece);
        return new ResponseEntity<>(p, headers, HttpStatus.OK);

    }
    @GetMapping("clients/typeP")
    @Timed
    public List<ClientAllDTO> findByTyprPer(){
        return clientService.findByTypePer();
    }
    @GetMapping("/clients/compte/{id}")
    @Timed
    public List<Compte> findCptEntrp(@PathVariable Long id) {
        return clientService.findCptEntrp(id);
    }
    @GetMapping("/clients/byTelephone")
    @Timed
    public ClientAllDTO getAllClientsByTelephoneAndCompte(@RequestParam String telephone, @RequestParam String numCompte, @RequestParam String codeService,
                                                          @RequestParam(required = false) String codeAgence,
                                                          @RequestParam(required = false) String rib,
                                                          @RequestParam(required = false) Long fseId,
                                                          @RequestParam(required = false) String codeTelephone) {
        log.debug("REST request to get a page of Clients");
        return clientService.findByClient(telephone, numCompte, codeService, codeAgence, rib, fseId, codeTelephone);
    }


    /**
     * GET  /clients/:id : get the "id" client.
     *
     * @param id the id of the clientDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the clientDTO, or with status 404 (Not Found)
     */
    @GetMapping("/clients/{id}")
    @Timed
    public ResponseEntity<ClientDTO> getClient(@PathVariable Long id) {
        log.debug("REST request to get Client : {}", id);
        ClientDTO clientDTO = clientService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(clientDTO));
    }
    @GetMapping("/clients/changeStatut/{id}")
    @Timed
    public ResponseEntity<ClientDTO> changeStatut(@PathVariable Long id) {
        log.debug("REST request to get Client : {}", id);
        ClientDTO clientDTO = clientService.changeStatut(id);
        
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(clientDTO));
    }
    @GetMapping("/clients/identifier/{id}")
    @Timed
    public ResponseEntity<ClientDTO> identifier(@PathVariable Long id) {
        log.debug("REST request to get Client : {}", id);
        ClientDTO clientDTO = clientService.identifier(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(clientDTO));
    }
    @GetMapping("/clients/{id}/setProfileActeur/{profileId}")
    @Timed
    public ResponseEntity<ClientDTO> setProfileActeur(@PathVariable Long id,@PathVariable Long profileId) {
        log.debug("REST request to get Client : {}", id);
        ClientDTO clientDTO = clientService.setProfileActeur(id,profileId);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(clientDTO));
    }

    @GetMapping("/clients/personne_search")
    @Timed
    public ClientAllDTO getClientByPersonne(@RequestParam Long id) {
        log.debug("REST request to get Client : {}", id);
        return clientService.findAllByPersonneId(id);
    }
    @GetMapping("/AllClients")
    @Timed
    public List<ClientAllDTO> findAllClients(@RequestParam(required = false) String nom, @RequestParam(required = false) String telephone, @RequestParam(required = false)String numeroCpt) {

        if(nom!=null && nom.equals("")){

            nom=null;
        }
        if(telephone!=null && telephone.equals("")){
            telephone=null;
        }
        if(numeroCpt!=null && numeroCpt.equals("")){
            numeroCpt=null;
        }
        return clientService.findAllClient(nom,telephone,numeroCpt);
    }
    @GetMapping("/AllClientsEntrp")
    @Timed
    public List<ClientAllDTO> findAllClientsEntrp(@RequestParam(required = false) String nom, @RequestParam(required = false) String telephone, @RequestParam(required = false)String numeroCpt) {
        if(nom!=null && nom.equals("")){
            nom=null;
        }
        if(telephone!=null && telephone.equals("")){
            telephone=null;
        }
        if(numeroCpt!=null && numeroCpt.equals("")){
            numeroCpt=null;
        }
        return clientService.findAllClientEntrp(nom,telephone,numeroCpt);
    }

    /**
     * DELETE  /clients/:id : delete the "id" client.
     *
     * @param id the id of the clientDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/clients/{id}")
    @Timed
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        log.debug("REST request to delete Client : {}", id);
        clientService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PostMapping("/saveCompteBanque-of-client")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN,AuthoritiesConstants.CREATE_COMPTE_BANQUE})
    public CompteBanque saveCompteBanqueOfClient(@RequestBody XCompteBanqueDTO compteBanque){
        log.debug("request to save compte banque of client"+compteBanque.toString());
        return clientService.saveCompteBanqueOfClient(compteBanque);
    }

    @DeleteMapping("/remove-compte-banque-of-client")
    @Timed
    public Boolean deleteCompteBanqueOfClient(@RequestParam Long compteBanqueId){
            log.debug("Request to delete compte banque of client"+compteBanqueId);
           return clientService.deleteCompteBanqueOfClient(compteBanqueId);
    }

    // TODO: 17/06/2022 Ressource ajouter et à revoir car d'autre existe déja réutiliser les résources existantes 

    /*@GetMapping("/clients-alls")
    @Timed
    public ResponseEntity<List<Client>> getAllClients(Pageable pageable) {
        log.debug("REST request to get all clients");
        Page<Client> page = clientService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/client");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }*/


    @GetMapping("/clients/filtrer/{telephone}")
    public ResponseEntity<List<Client>> getClientsFilter(Pageable pageable, @PathVariable String telephone) {
        log.debug("REST request to get a page of clients filtered");
        Page<Client> page = clientService.findByTypePersonnePage(TypePersonne.PHYSIQUE,null,telephone,null,null,null,null,null,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,"/api/client");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/clients/filtrer-list/{telephone}")
    @Timed
    public List<ClientAllDTO> getAllclients(@PathVariable String telephone) {
        log.debug("REST request to get all clients");
        if(telephone !=null){
            List<ClientAllDTO> lists = clientService.findAllClient(null,telephone,null);
            return lists;
        }
        return null;
    }

    @GetMapping("/client/{id}")
    @Timed
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        log.debug("REST request to get Client : {}", id);
        ClientDTO client = clientService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(client));
    }
    @GetMapping("/clients/type-optimise")
    @Timed
    public ResponseEntity<List<ClientAllDTO>> findByTypePersonneOptimise(@RequestParam(required = true) TypePersonne typePersonne,@RequestParam(required = false) String code, @RequestParam(required = false) String telephone, @RequestParam(required = false) String adresse,@RequestParam(required = false)String nom,@RequestParam(required = false)String prenom,@RequestParam(required = false) String raisonSociale,@RequestParam(required = false) String numeroPiece, Pageable pageable) {
        log.debug("REST request to get a page of Clients");
        Page<ClientDTO> page=clientService.findByTypePersonnePageOptimise(typePersonne,code,telephone,adresse,nom,prenom,raisonSociale,numeroPiece,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/clients/type-optimise");
        List<ClientAllDTO> p=clientService.getAllDTOOptimise(page,numeroPiece);
        return new ResponseEntity<>(p, headers, HttpStatus.OK);

    }

    @GetMapping("/client-count")
    @Timed
    public HashMap countClient(@RequestParam(required = true) TypePersonne typePersonne){
        log.debug("Request to count client----");
        return clientService.findByTypePersonne(typePersonne);
    }

    @GetMapping("/add-compte-como")
    @Timed
    public  Compte ajoutCompteComo(@RequestParam Long clientId){
        ClientDTO client=clientService.findOne(clientId);
        if(client!=null && client.getId()!=null) {
            return clientService.addCompteComo(client);
        }
        throw  new CustomParameterizedException("Client innexistant!");
    }



}
