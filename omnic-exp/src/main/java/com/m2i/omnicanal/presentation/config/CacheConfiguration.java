package com.m2i.omnicanal.presentation.config;

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

@Configuration
@EnableCaching
@AutoConfigureAfter(value = {MetricsConfiguration.class})
@AutoConfigureBefore(value = {WebConfigurer.class, DatabaseConfiguration.class})
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        BeanClassLoaderAwareJCacheRegionFactory.setBeanClassLoader(this.getClass().getClassLoader());
        JHipsterProperties.Cache.Ehcache ehcache
                = jHipsterProperties.getCache().getEhcache();

        CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(new CacheLogger(), EventType.CREATED, EventType.UPDATED, EventType.EVICTED)
                .unordered().asynchronous();
        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                        ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(ehcache.getTimeToLiveSeconds(), TimeUnit.SECONDS)))
                        .add(cacheEventListenerConfiguration)
                        .build());
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
                        cm.createCache("findCompteFse", jcacheConfiguration);

            cm.createCache("findByCodeAgenceAndFournisseurServiceId", jcacheConfiguration);

            cm.createCache("findUoByCodeFse", jcacheConfiguration);
            cm.createCache("fseCompense", jcacheConfiguration);
            cm.createCache("agenceSiege", jcacheConfiguration);
          
            cm.createCache("compiledSchema", jcacheConfiguration);
            cm.createCache("interfaceUser", jcacheConfiguration);
            cm.createCache("schemaDecision", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.Etiquette.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.User.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.Authority.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.SchemaEcriture.class.getName(), jcacheConfiguration);
            //cm.createCache(com.m2i.omnicanal.presentation.domain.User.class.getName() + ".authorities", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.SchemaEcrit.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.LigneSchemaEcrit.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.ParametreSchem.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.ParametreMethod.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.CompteFse.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.MethodeFonction.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.ParametreUser.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.LigneSchemaEcrit.class.getName() + ".constitues", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.PaysFse.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.ServiceMethod.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.InterfaceUser.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.FournisseurFSE.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.ApplicationApi.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.ApplicationApi.class.getName() + ".fournisseurService", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.FournisseurFSE.class.getName() + ".fournits", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.Service.class.getName(), jcacheConfiguration);
            
            
                        cm.createCache(com.m2i.omnicanal.presentation.domain.RelationFse.class.getName(), jcacheConfiguration);

            //     cm.createCache(com.m2i.omnicanal.presentation.domain.Transaction.class.getName(), jcacheConfiguration);
            //  cm.createCache(com.m2i.omnicanal.presentation.domain.TypeTransaction.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.presentation.domain.Parameters.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.ServiceTraitement.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.LigneSchemaEcriture.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.LigneSchemaEcriture.class.getName() + ".constitues", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Methode.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.FournisseurService.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Api.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Parametre.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.ParametreUtilisateur.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.InterfaceUtilisateur.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Compte.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.ParametreMethode.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Api.class.getName() + ".fournisseurService", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Api.class.getName() + ".constitues", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.FournisseurService.class.getName() + ".fournits", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.InterfaceUtilisateur.class.getName() + ".constitues", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.FournisseurService.class.getName() + ".realisepars", jcacheConfiguration);

            // jhipster-needle-ehcache-add-entry
        };
    }
}
