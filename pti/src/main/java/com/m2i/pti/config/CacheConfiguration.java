package com.m2i.pti.config;

import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.EntityAuditEvent;
import com.m2i.pti.domain.ParametreExport;
import com.m2i.pti.domain.User;
import com.m2i.pti.partenaire.domain.FactureOnea;
import io.github.jhipster.config.JHipsterProperties;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.jsr107.Eh107Configuration;

import java.util.concurrent.TimeUnit;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.event.EventType;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;

@Configuration
@EnableCaching
@AutoConfigureAfter(value = {MetricsConfiguration.class})
@AutoConfigureBefore(value = {WebConfigurer.class, DatabaseConfiguration.class})
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        //  BeanClassLoaderAwareJCacheRegionFactory.setBeanClassLoader(this.getClass().getClassLoader());
        JHipsterProperties.Cache.Ehcache ehcache
                = jHipsterProperties.getCache().getEhcache();
        CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(new CacheLogger(), EventType.CREATED, EventType.UPDATED, EventType.EVICTED)
                .unordered().asynchronous();
        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                        ResourcePoolsBuilder.heap(ehcache.getMaxEntries())).add(cacheEventListenerConfiguration)
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(ehcache.getTimeToLiveSeconds(), TimeUnit.SECONDS)))
                        .build());
    }
  @Bean("ehcacheCacheManager")


    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            cm.createCache("findPointByCode", jcacheConfiguration);
            cm.createCache("SoldeCompte1", jcacheConfiguration);
            cm.createCache("SoldeCompte", jcacheConfiguration);
            cm.createCache(EntityAuditEvent.class.getName(), jcacheConfiguration);
            cm.createCache(User.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Ville.class.getName(), jcacheConfiguration);
            cm.createCache(Authority.class.getName(), jcacheConfiguration);
            cm.createCache(User.class.getName() + ".authorities", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.OauthClientDetails.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ChargementCompte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Carte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Bouton.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneChargement.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneChargementCompte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.OperationSuivis.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.BlackList.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Liste.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Commerciale.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisationCorrespondance.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.ActiviteClient.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.RecepteurEmail.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.Produit.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ReglementTemplate.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Produit.class.getName() + ".ecrans", jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.ExportTable.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ParametreExport.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.FileExport.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.FileExportParam.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ExternalAccount.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.FacturierProfils.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.BackupData.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".tableauFraisEms", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".tableauFraisAcqs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".comptes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".autorisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".pointVentes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeUniteOrganisation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeUniteOrganisation.class.getName() + ".tableauFraisEms", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeUniteOrganisation.class.getName() + ".tableauFraisAcqs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeUniteOrganisation.class.getName() + ".organisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Personne.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Personne.class.getName() + ".pieces", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Personne.class.getName() + ".emetteurs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Personne.class.getName() + ".beneficiaires", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Personne.class.getName() + ".uniteOrganisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Piece.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypePiece.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypePiece.class.getName() + ".pieces", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.PersonnePhysique.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Nationalite.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Nationalite.class.getName() + ".personnePhysiques", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.PersonneMorale.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Emetteur.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Emetteur.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Beneficiaire.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Beneficiaire.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Client.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Client.class.getName() + ".comptes", jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.TableOfData.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.Client.class.getName() + ".notifications", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeServiceFse.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Client.class.getName() + ".documents", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Client.class.getName() + ".tableauFraisBens", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Client.class.getName() + ".tableauFraisEms", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Compte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeCompte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeCompte.class.getName() + ".comptes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.CompteBanque.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Devise.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Devise.class.getName() + ".compteBanques", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.CompteAutre.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.EtiquetteCompte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TableauFrais.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TableauFrais.class.getName() + ".ligneFrais", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneFrais.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Canal.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Canal.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Canal.class.getName() + ".tableauFrais", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Canal.class.getName() + ".typeServices", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Operation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.MotifOperation.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".tableauFrais", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".tableauRepartitionCommissions", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".autorisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".canals", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.PointVente.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.PointVente.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.FournisseurService.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.FournisseurService.class.getName() + ".organisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TableauRepartitionCommission.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TableauRepartitionCommission.class.getName() + ".ligneRepartitionCommissions", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneRepartitionCommission.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Autorisation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Pays.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ParamCanal.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.CategorieService.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Pays.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Pays.class.getName() + ".fournisseurServices", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.EtiquetteComptable.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Devise.class.getName() + ".pays", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".filsUOS", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.User.class.getName() + ".autorisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".utilisateurs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.PointVente.class.getName() + ".utilisateurs", jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".operationsEffectue", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.RepartionFraisEmAcq.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Commission.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Operation.class.getName() + ".commissions", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".repartitions", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".repartitionsFrais", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".tableauRepartitionCommissions", jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.Operation.class.getName() + ".audits", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.User.class.getName() + ".audits", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.OperationAudit.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Profil.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeUniteOrganisation.class.getName() + ".roles", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Profil.class.getName() + ".roles", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Profil.class.getName() + ".users", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".profils", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".commissions", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".instructions", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.InstructionComptable.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Compte.class.getName() + ".compteBases", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Compte.class.getName() + ".transactionComptePtis", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TransactionComptePti.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.CompteAutre.class.getName() + ".transactionCompteAutres", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.CompteAutre.class.getName() + ".transactionComptePtis", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Compte.class.getName() + ".operationSrcs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Compte.class.getName() + ".operationDests", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.User.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".machines", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".taxes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".taxesZone", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Machine.class.getName() + ".audits", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.BrancheActivite.class.getName() + ".personnes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TaxeOperation.class.getName() + ".typeServices", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".serviceProfils", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TaxeOperation.class.getName() + ".operations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Operation.class.getName() + ".taxes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeService.class.getName() + ".taxes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Machine.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Terminal.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.MachineAuditEvent.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ParametreApp.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.MotifOperation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.BrancheActivite.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Taxe.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TaxeOperation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Taxe.class.getName() + ".taxeOperations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TaxeOperation.class.getName() + ".bilanTaxes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Operation.class.getName() + ".bilanTaxes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".uoDevises", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Devise.class.getName() + ".uoDevises", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ServiceProfil.class.getName() + ".clients", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UniteOrganisation.class.getName() + ".villes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.BilanTaxe.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TauxChange.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UoDevise.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.Ville.class.getName() + ".pays", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TableauCumul.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TableauCumul.class.getName() + ".ligneFrais", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.BilanOperation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.RepartitionFrais.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.RepartitionOp.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.RegleCommission.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ServiceProfil.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ServiceProfil.class.getName() + ".typeServices", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".acteurs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".clients", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".tableauFrais", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".organisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".tableauCumuls", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".groupRepartitions", jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".tableauRepartitionCommissions", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeurTableauRepartitionCommission.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeurTableauFrais.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeurTableauCumul.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeurHoraireAuthentification.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.HoraireAuthentification.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.HoraireAuthentification.class.getName() + ".profilActeurs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProfilActeur.class.getName() + ".horaireAuthentifications", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeDocument.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Document.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LogOperation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.CategorieClient.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Composant.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Ecran.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.StructureOrganisationFacturier.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ClassificationFacturier.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeReclamation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ChargementTransfert.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.EvenementOperation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneChargementTransfert.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Reclamation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Champs.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Facture.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Facturier.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TemplateMessage.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.enumeration.Langue.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.HistExecutionJob.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.PtiJobDetails.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.PtiJobDetails.class.getName() + ".histExecutionJobs", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.RoleValidation.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.GroupRepartition.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.GroupRepartition.class.getName() + ".tableauRepartitionCommissions", jcacheConfiguration);

            cm.createCache(FactureOnea.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.GroupeProfilActeur.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ReleveCompte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ReleveCompte.class.getName() + ".detailReleveComptes", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.DetailReleveCompte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.UserControl.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.RecepteurAlarme.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.CredentialApi.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Reglement.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneReglement.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneReglement.class.getName() + ".uniteOrganisations", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ChargementMessage.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.LigneMessage.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Historique.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.DetailHistorique.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Faq.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Lien.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Utilitaire.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Localite.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.TypeLocalite.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Localite.class.getName() + ".typeLocalites", jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ProduitCanal.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.CanalApplication.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.Ftp.class.getName(), jcacheConfiguration);

            cm.createCache(com.m2i.pti.domain.CanalCommunication.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.VerifOtp.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.ParamExecutionJob.class.getName(), jcacheConfiguration);// jhipster-needle-ehcache-add-entry
            cm.createCache(com.m2i.pti.domain.DetailsLot.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Lot.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.Publicite.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.InfoSupp.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.pti.domain.HistoriqueGroupeProfilActeur.class.getName(), jcacheConfiguration);
        };
    }
}
