/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.service;

import com.m2i.pti.domain.BwCustomer;
import com.m2i.pti.domain.Carte;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.external.domain.ExtBwCustomer;
import com.m2i.pti.mobile.dto.MClient;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.mobile.dto.MResponseOperation;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.BwAccountRepository;
import com.m2i.pti.repository.BwCustomerRepository;
import com.m2i.pti.repository.ClientRepository;
import com.m2i.pti.repository.CompteAutreRepository;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.CompteService;

import com.m2i.pti.domain.CompteBanque;
import com.m2i.pti.domain.ExternalAccount;

import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.CarteRepository;
import com.m2i.pti.repository.ExternalAccountRepository;

import com.m2i.pti.repository.CompteBanqueRepository;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.dto.Balance;
import com.m2i.pti.service.dto.Mouvement;
import com.m2i.pti.service.dto.Statement;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import jakarta.persistence.NonUniqueResultException;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 *
 * @author DA S Cyrille Patrick (CDA)
 */
@Service
public class ExtService {

    BwAccountService bwAccountService;
    BwCustomerService bwCustomerService;
    UniteOrganisationRepository uniteOrganisationRepository;
    BwCustomerRepository bwCustomerRepository;
    BwAccountRepository bwAccountRepository;
    private final Logger log = LoggerFactory.getLogger(ExtService.class);
    ApiService apiService;
    MobileService mobileService;
    ClientRepository clientRepository;
    CompteService compteService;
    CompteAutreRepository compteAutreRepository;
    CompteRepository compteRepository;
    CompteBanqueRepository compteBanqueRepository;
    UniteOrganisationService uniteOrganisationService;
    CarteRepository carteRepository;
    ExternalAccountRepository externalAccountRepository;

    public ExtService(ExternalAccountRepository externalAccountRepository, CarteRepository carteRepository, UniteOrganisationService uniteOrganisationService, UniteOrganisationRepository uniteOrganisationRepository, CompteBanqueRepository compteBanqueRepository, CompteRepository compteRepository, CompteAutreRepository compteAutreRepository, CompteService compteService, ClientRepository clientRepository, MobileService mobileService, BwCustomerRepository bwCustomerRepository, BwAccountService bwAccountService, BwCustomerService bwCustomerService, ApiService apiService, BwAccountRepository bwAccountRepository) {
        this.bwAccountService = bwAccountService;
        this.bwCustomerService = bwCustomerService;
        this.bwCustomerRepository = bwCustomerRepository;
        this.apiService = apiService;
        this.bwAccountRepository = bwAccountRepository;
        this.mobileService = mobileService;
        this.compteService = compteService;
        this.clientRepository = clientRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.compteRepository = compteRepository;
        this.compteBanqueRepository = compteBanqueRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.carteRepository = carteRepository;
        this.externalAccountRepository = externalAccountRepository;
    }

    @Transactional
    public MResponse deleteBwCustomer(ExtBwCustomer cust) {
        BwCustomer bw = bwCustomerService.findByTelephone(cust.getMsisdn());
        if (bw != null) {
            bw.setValide(false);
            BwCustomer bwsave = bwCustomerService.save(bw);
            if (bwsave != null) {
                return new MResponse("0", "Opération terminée avec succès!");
            }
        }
        return new MResponse("-1", "operation impossible ");
    }

    private String getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, 22);
        String valeur = String.format("%22d", crc32.getValue());

        return valeur;
    }

    private Compte saveCompteExt(Personne personne, String codeFse, MClient mclient, boolean activeCpt) {
        try {
            String alias = null;
            String codeOperateur = null;
            Compte compte;
            boolean compteExternalExist = false;
            Compte cpte = null;
            MCompte mcompte = mclient.getCompte();
            Client clientExist = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
            if (clientExist != null) {
                //     if (mcompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                if (mcompte.getAlias() != null && mcompte.getAlias().length() > 0) {
                    alias = mcompte.getAlias();
                    codeOperateur = mcompte.getCodeOperateur();
                    ExternalAccount externalAccount = externalAccountRepository.findExternalAccountByAliasAndCodeOperateur(alias, mcompte.getCodeOperateur());
                    if (externalAccount != null && externalAccount.getCompte() != null) {
                        cpte = externalAccount.getCompte();
                        compteExternalExist = true;
                    } else {
                        compteExternalExist = false;
                        cpte = compteRepository.findByValeurCompteBanqueAndCodeFseClient(mcompte.getNumeroCompte(), codeFse, clientExist.getId());
                    }
                    /* else {
                            compteExternalExist = false;
                            cpte = compteRepository.findByValeurCompteBanqueAndCodeFseAndAlias(codeFse, mcompte.getAlias());
                           
                        }*/

                } else {
                    throw new CustomParameterizedException("Code opérateur ou alias non valide");
                }

                //   } else {
                //   cpte = compteService.findByCompteFse(mcompte.getNumeroCompte(), codeFse);
                // }
                if (cpte == null) {
                    compte = new Compte();
                    compte.valeurCompte(mcompte.getNumeroCompte())
                            .client(clientExist)
                            .etiquette(EtiquetteCompteCode.CPTECLIENT.toString());
                    compte.setAlias(mcompte.getAlias());
                    System.out.println(codeFse);
                    UniteOrganisation fseTeneur = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFse);
                    if (fseTeneur != null) {
                        if (mcompte.getTypeCompte().equals(MTypeCompte.AUTRE)) {
                            compte.categorieCompte(CategorieCompte.AUTRE);
                            CompteAutre compteAutre = new CompteAutre();
                            compteAutre.setCodeFse(codeFse);
                            compteAutre.setSolde(BigDecimal.ZERO);
                            compteAutre = compteAutreRepository.save(compteAutre);
                            compte.compteAutre(compteAutre);
                        } else if (mcompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                            compte.categorieCompte(CategorieCompte.BANQUE);
                            CompteBanque compteBanque = new CompteBanque();
                            compteBanque.setCodeAgence(mcompte.getCodeAgence());
                            compteBanque.codeBanque(mcompte.getCodeBanque());
                            compteBanque.setRib("0");
                            compteBanque.setCodeFse(codeFse);
                            //   String concatCpt=mcompte.getCodeBanque()+mcompte.getCodeAgence()+mcompte.getNumeroCompte();
                            //  getCRC32Checksum(concatCpt.getBytes());
                            compteBanque.setAlias(mcompte.getAlias());
                            compteBanque = compteBanqueRepository.save(compteBanque);
                            compte.compteBanque(compteBanque);
                        } else if (mcompte.getTypeCompte().equals(MTypeCompte.CARTE)) {
                            compte.categorieCompte(CategorieCompte.CARTE);
                            compte.setStatutCompte(StatutCompte.DESACTIVE);
                            Carte carte = new Carte();
                            carte.setCodeFse(codeFse);
                            carte.setSolde(BigDecimal.ZERO);
                            carte.setLast4Digit(mcompte.getLas4Digit());
                            carte = carteRepository.save(carte);
                            compte.setCarte(carte);

                        }
                        if (activeCpt) {
                            compte.setStatutCompte(StatutCompte.ACTIVE);
                        } else {
                            compte.setStatutCompte(StatutCompte.DESACTIVE);
                        }

                        compte.setEtatCompte(EtatCompte.OUVERT);
                        Compte cpteSave = compteRepository.save(compte);
                        if (cpteSave != null) {
                            compte = cpteSave;
                        }

                    }

                } else {

                    log.error("le compte existe déjà.");
                    compte = cpte;

                    // throw new CustomParameterizedException("le compte existe déjà.");
                }
            } else {
                compte = cpte;
                log.error("le client n' existe pas");
            }
            /**
             * enregistrement du compte externe
             */
            if ((compteExternalExist == false)) {
                saveCompteExternal(compte, codeOperateur, alias);

            }

            ///                throw new CustomParameterizedException("le compte existe dÃ©jÃ .");
            return compte;
        } catch (CustomParameterizedException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    private void saveCompteExternal(Compte cpte, String codeOperateur, String alias) {
        if (cpte != null && codeOperateur != null && alias != null && alias.length() > 0) {
            ExternalAccount externalAccount = new ExternalAccount();
            externalAccount.setCompte(cpte);
            externalAccount.setCodeOperateur(codeOperateur);
            externalAccount.setAlias(alias);
            externalAccount.setEtatCompteExterne(true);
            externalAccountRepository.save(externalAccount);
        } else {
            throw new CustomParameterizedException("Code opérateur ou alias non valide");
        }

    }

    public Compte creationCompte(MClient client, Personne personne, boolean activeImmediat) {
        log.info(" création du compte......");
        String codeFse = client.getCodeFse();
        return saveCompteExt(personne, codeFse, client, activeImmediat);
    }

    public MInfos getMPersonne(ExtBwCustomer extCustomer, String codeOperateur) {
        String codeBanque = "0";
        String codeAgence = "0";
        String numeroTelephone = "";
        String teneur;
        String numeroCompte = "";
        MInfos minfos = null;
        numeroTelephone = extCustomer.getMsisdn();
        teneur = extCustomer.getCodeFse();
        numeroCompte = extCustomer.getNumeroCompte();
        if (extCustomer.getTypeCompte().equals("BANQUE")) {
            codeBanque = extCustomer.getCodeBanque();
            codeAgence = extCustomer.getCodeAgence();
        } else if (extCustomer.getTypeCompte().equals("CARTE")) {
            codeAgence = extCustomer.getLast4Digit();
        }
        String malias = compteService.getCRC32Checksum(codeAgence, numeroCompte, numeroTelephone, codeOperateur);
        minfos = compteService.findByalias(malias);
        //   Compte compte = mobileService.findComptePersonne(telephone, codePays);
        // Compte compte = compteRepository.findByClientAndValeurCompte(client, telephone);
        if (minfos != null) {
            return minfos;
        } else {
            throw new CustomParameterizedException("Compte introuvable !");
        }

    }

    public MResponse doSouscription(MClient client, boolean activeCpt) {
        String message;
        Personne personne = mobileService.savePersonne(client.getPersonne());
        //  getCompte(client.getCompte(), personne);
        //   saveNewCompte(client.getCompte(), personne);
        if (personne != null) {
            /**/
            Client c = mobileService.saveClient(client, personne);
            if (c != null) {
                Compte cpt = creationCompte(client, personne, activeCpt);
                if (cpt != null) {
                    if (cpt.getCompteBanque() != null) {
                        return new MResponse("0", "Opération effectuée avec succès.", cpt.getCompteBanque().getAlias());
                    } else {
                        return new MResponse("0", "Opération effectuée avec succès.");
                    }

                } else {
                    return new MResponse("-1", "Impossible de créer le compte, ou le compte est déjà lié");
                }
            } else {
                return new MResponse("-1", "Erreur lors de la création du compte");
            }
        } else {
            return new MResponse("0", "le client existe ou Erreur lors de la création du compte");
            // throw new CustomParameterizedException("Impossible d'enrï¿½ler ce client.");
        }

    }

    public MResponse delinkBwCustomer(ExtBwCustomer cust, String codefse) {
        MResponse rep = verifyLink(cust, cust.getMsisdn(), codefse);
        Compte cpte;
        if (rep != null && rep.getCode().equals("0")) {
            if (cust.getAlias() != null) {
                cpte = compteRepository.findByValeurCompteBanqueAndCodeFseAndAlias(codefse, cust.getAlias());
            } else {
                cpte = compteRepository.findByValeurCompteBanqueAndCodeFse(cust.getNumeroCompte(), codefse);
            }

            if (cpte == null) {
                cpte = compteRepository.findByValeurCompteAndCodeFse(cust.getNumeroCompte(), codefse);
            }
            if (cpte != null) {
                //cpte.setEtatCompte(EtatCompte.FERME);
                cpte.setStatutCompte(StatutCompte.DESACTIVE);
                compteRepository.save(cpte);
            } else {
                return new MResponse("-1", "Le compte n'existe pas");
            }
        } else {
            return new MResponse("-1", "Aucune Liaison pour ce compte");
        }

        return new MResponse("0", "Opération effectuée avec success");
    }

    /**
     *
     * @param cust
     * @param codefse
     * @param activeImmediat active immediat pour activer immediatement
     * @return
     */
    @Transactional
    public MResponse createBwCustomer(ExtBwCustomer cust, String codefse, boolean activeImmediat) {
        // BwCustomer bw = bwCustomerService.findByTelephone(cust.getMsisdn());
        UniteOrganisation uo = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codefse);
        if (uo != null) {
            UniteOrganisationDTO uoPays = uniteOrganisationService.findPereByType(uo.getId(), TypeUO.PAYS);
            if (uoPays == null) {
                return new MResponse("-1", "Erreur lors de la création");
            }
            MClient client = new MClient();
            MPersonne mPersonne = new MPersonne();
            //Remplissage de MCompte
            //Remplissage de l'objet MPersonne
            mPersonne.setCodeTelephone(cust.getCodePays());
            mPersonne.setTelephone(cust.getMsisdn());
            mPersonne.setPrenom(cust.getPrenomCustomer());
            mPersonne.setNom(cust.getNomCustomer());
            MCompte mCompte = new MCompte();
            //Remplissage de MCompte
            mCompte.setNumeroCompte(cust.getNumeroCompte());
            mCompte.setCodeAgence(cust.getCodeAgence());
            mCompte.setAlias(cust.getAlias());
            mCompte.setCodeOperateur(cust.getCodeOperateur());
            // mCompte.setCodeBanque(cust.getCodeBanque());
            String codeAgence = "0";
            if (cust != null && cust.getCodeAgence() == null) {
                codeAgence = "0";
            } else {
                codeAgence = cust.getCodeAgence();
            }
            if (cust.getTypeCompte() != null) {
                mCompte.setTypeCompte(MTypeCompte.valueOf(cust.getTypeCompte()));
                if (cust.getTypeCompte().equals("BANQUE")) {
                    mCompte.setCodeBanque(uo.getCodeBanque());

                } else if (cust.getTypeCompte().equals("CARTE")) {
                    mCompte.setLas4Digit(cust.getLast4Digit());
                }
            } else {
                if (codeAgence.equals("0")) {
                    mCompte.setTypeCompte(MTypeCompte.AUTRE);
                } else {
                    mCompte.setTypeCompte(MTypeCompte.BANQUE);
                    mCompte.setCodeBanque(uo.getCodeBanque());

                }
            }
            mCompte.setTeneurCompte(codefse);
            client.setPersonne(mPersonne);
            client.setCodeFse(codefse);
            client.setCompte(mCompte);
            MResponse rep = doSouscription(client, activeImmediat);
            return rep;
        }


        /*if (bw == null) {
            BwCustomer bwNew = new BwCustomer();
            bwNew.setCodePays(cust.getCodePays());
            bwNew.setMsisdn(cust.getMsisdn());
            bwNew.setNomCustomer(cust.getNomCustomer());
            bwNew.setNumeroPiece(cust.getNumeroPiece());
            bwNew.setPrenomCustomer(cust.getPrenomCustomer());
            bwNew.setTypePiece(cust.getTypePiece());
            bwNew.setUniteOrganisation(uo);
            bwNew.setValide(false);
            BwCustomer bwsave = bwCustomerService.save(bwNew);*/
        // if (bwsave != null) {
        /* BwAccount bwAccount = new BwAccount();
                bwAccount.setCodeAgence(cust.getCodeAgence());
                bwAccount.setCodeBanque(cust.getCodeBanque());
                bwAccount.setCodeFse(codefse);*/
        //   bwAccount.setBwCustomer(bwsave);
        // bwAccount.setNumeroCompte(cust.getNumeroCompte());
        // BwAccount bwaccountsave = bwAccountService.save(bwAccount);
        //    if (bwaccountsave != null) {
        // return new MResponse("0", "Opération terminée avec succès!");
        //  }
        // }
        //}
        return new MResponse("-1", "Erreur lors de la création");
    }

    public ExtBwCustomer returnCustomer(String numeroCompte, String msisdn, String codefse, String alias) {
        log.debug("REST request to verify linked : {}");
        ExtBwCustomer customer = null;
        Compte cpte = null;
        if (alias != null && alias.length() > 0) {
            cpte = compteRepository.findByValeurCompteBanqueAndCodeFseAndAlias(codefse, alias);
        } else {
            cpte = compteRepository.findByValeurCompteBanqueAndCodeFse(numeroCompte, codefse);
        }
        if (cpte == null) {
            cpte = compteRepository.findByValeurCompteAndCodeFse(numeroCompte, codefse);
        }
        String codeagence = "0";
        String codebanque = "0";
        if (cpte != null) {
            if (cpte.getCompteAutre() != null) {
                codebanque = "0";
                codeagence = "0";
            } else if (cpte.getCompteBanque() != null) {
                codebanque = cpte.getCompteBanque().getCodeBanque();
                codeagence = cpte.getCompteBanque().getCodeAgence();
            }
            if (cpte.getClient() != null && cpte.getClient().getPersonne() != null && msisdn.equals(cpte.getClient().getPersonne().getTelephone()) && cpte.getStatutCompte().equals(StatutCompte.ACTIVE)) {
                customer = new ExtBwCustomer();
                customer.setCodeAgence(codeagence);
                customer.setCodeBanque(codebanque);
                customer.setNumeroCompte(cpte.getValeurCompte());
                customer.setCodeFse(codefse);
                customer.setMsisdn(msisdn);
                if (cpte.getClient().getPersonne().getTypePersonne().equals(TypePersonne.PHYSIQUE) && cpte.getClient().getPersonne().getPersonnePhysique() != null) {
                    customer.setNomCustomer(cpte.getClient().getPersonne().getPersonnePhysique().getNomClient());
                    customer.setPrenomCustomer(cpte.getClient().getPersonne().getPersonnePhysique().getPrenomClient());
                } else if (cpte.getClient().getPersonne().getTypePersonne().equals(TypePersonne.MORALE) && cpte.getClient().getPersonne().getPersonneMorale() != null) {
                    customer.setNomCustomer(cpte.getClient().getPersonne().getPersonneMorale().getRaisonSociale());
                    customer.setPrenomCustomer("");
                }

            }
        }

        return customer;
    }

    @Transactional
    public MInfos getMPersonne(String alias, String codeTelephone) {
        MCompte mCompte = new MCompte();
        String codeFse = "0";

        ExtBwCustomer extbw = null;//returnCustomer(numeroCompte, msisdn, codeFse);
        if (extbw != null) {
            // Compte compte = compteRepository.findByClientAndValeurCompte(client, telephone);
            MInfos mInfos = new MInfos();
            //Remplissage de MInfo

            MPersonne mPersonne = new MPersonne();
            //Remplissage de MCompte
            mCompte.setNumeroCompte(extbw.getNumeroCompte());

            mCompte.setTeneurCompte(extbw.getCodeFse());
            //Remplissage de l'objet MPersonne
            mPersonne.setTelephone(extbw.getMsisdn());
            mPersonne.setPrenom(extbw.getPrenomCustomer());
            mPersonne.setNom(extbw.getNomCustomer());
            mPersonne.setCodeTelephone(codeTelephone);
            mInfos.setCompte(mCompte);
            mInfos.setTitulaire(mPersonne);
            return mInfos;

        } else {
            throw new CustomParameterizedException("Client introuvable !");
        }
    }

    @Transactional
    public MInfos getMPersonne(String numeroCompte, String msisdn, Compte compteSrc, String codeTelephone, String alias) {
        MCompte mCompte = new MCompte();
        String codeFse = "0";
        if (compteSrc.getCompteAutre() != null) {
            mCompte.setTypeCompte(MTypeCompte.AUTRE);
            codeFse = compteSrc.getCompteAutre().getCodeFse();
        } else if (compteSrc.getCompteBanque() != null) {
            mCompte.setTypeCompte(MTypeCompte.BANQUE);
            mCompte.setCodeAgence(compteSrc.getCompteBanque().getCodeAgence());
            mCompte.setCodeBanque(compteSrc.getCompteBanque().getCodeBanque());
            codeFse = compteSrc.getCompteBanque().getCodeFse();
        }
        ExtBwCustomer extbw = returnCustomer(numeroCompte, msisdn, codeFse, alias);
        if (extbw != null) {
            // Compte compte = compteRepository.findByClientAndValeurCompte(client, telephone);
            MInfos mInfos = new MInfos();
            //Remplissage de MInfo

            MPersonne mPersonne = new MPersonne();
            //Remplissage de MCompte
            mCompte.setNumeroCompte(extbw.getNumeroCompte());

            mCompte.setTeneurCompte(extbw.getCodeFse());
            //Remplissage de l'objet MPersonne
            mPersonne.setTelephone(extbw.getMsisdn());
            mPersonne.setPrenom(extbw.getPrenomCustomer());
            mPersonne.setNom(extbw.getNomCustomer());
            mPersonne.setCodeTelephone(codeTelephone);
            mInfos.setCompte(mCompte);
            mInfos.setTitulaire(mPersonne);
            return mInfos;

        } else {
            throw new CustomParameterizedException("Client introuvable !");
        }
    }

    public MResponse verifyLink(ExtBwCustomer cust, String msisdn, String codefse) {
        log.debug("REST request to verify linked : {}");
        Compte cpte;
        String numeroCompte = cust.getNumeroCompte();
        if (cust.getAlias() != null && cust.getAlias().length() > 0 && !cust.getAlias().equals("")) {
            cpte = compteRepository.findByValeurCompteBanqueAndCodeFseAndAlias(codefse, cust.getAlias());
            if (cpte == null) {
                cpte = compteRepository.findByAlias(cust.getAlias());
            }
        } else {
            cpte = compteRepository.findByValeurCompteBanqueAndCodeFse(cust.getNumeroCompte(), codefse);
        }
        MResponse mResponse = new MResponse("-11", "liaison inexistante...!");
        //*si le compte banque est null alors
        if (cpte == null) {
            cpte = compteRepository.findByValeurCompteAndCodeFse(numeroCompte, codefse);
        }
        if (cpte != null) {
            if (cpte.getClient() != null && cpte.getClient().getPersonne() != null && msisdn.equals(cpte.getClient().getPersonne().getTelephone()) && cpte.getStatutCompte().equals(StatutCompte.ACTIVE)) {
                log.debug("Liaison effective: {}");
                mResponse = new MResponse("0", "liaison existante...!");
            } else if (cpte.getStatutCompte().equals(StatutCompte.DESACTIVE)) {
                mResponse = new MResponse("-1", "liaison non encore validé!");
            }
        }
        return mResponse;
    }

    public List<Mouvement> getMiniStatement(String agence, String numeroCompte, String codeFse, String debutPeriode, String finPeriode) throws IOException, JAXBException {
        Statement statement = apiService.getHistorique(numeroCompte, agence, codeFse, debutPeriode, finPeriode);
        if (statement != null) {
            System.out.println("--------------------statement.getListeMouvement()--------------------------");
            System.out.println(statement.getListeMouvement().size());
            return statement.getListeMouvement();
        } else {
            return Collections.emptyList();
        }

    }

    @Transactional
    public Balance getSolde(String numeroCompte, String agence, String codeFse) throws IOException, JAXBException {
        Balance balance = apiService.getSolde(numeroCompte, agence, codeFse);
        return balance;
    }

    public MResponseOperation doAnnulation(String codeOperation, String codePv) throws IOException, NoSuchAlgorithmException, JAXBException {

        MResponseOperation mResponse = mobileService.doannulationOperation(codeOperation, codePv);
        // ResponseEntity rep = apiService.virementSimple(compteSrc, compteDest, montantOp);

        return mResponse;
    }

    public MResponseOperation doTransfert(MOperation mOperation, String codePv, Compte compteSrc, Compte compteDest, Compte compteComm, BigDecimal montantOp, String reference, String api, String description, String donneurOrdre, String beneficiaire, String alias) throws IOException, NoSuchAlgorithmException, JAXBException {

        MResponseOperation mResponseOperation = mobileService.doOperationServiceExterieur(mOperation, codePv, compteSrc, compteDest, compteComm, montantOp, reference, api, description, donneurOrdre, beneficiaire, alias);
        // ResponseEntity rep = apiService.virementSimple(compteSrc, compteDest, montantOp);
        return mResponseOperation;
    }

}
