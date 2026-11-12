package com.m2i.omnicanal.application.config;

import io.github.jhipster.config.JHipsterProperties;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.jsr107.Eh107Configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
@AutoConfigureAfter(value = { MetricsConfiguration.class })
@AutoConfigureBefore(value = { WebConfigurer.class, DatabaseConfiguration.class })
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache =
            jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(ehcache.getTimeToLiveSeconds(), TimeUnit.SECONDS)))
                .build());
    } 
 
    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            cm.createCache(com.m2i.omnicanal.application.domain.User.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.application.domain.Authority.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.application.domain.User.class.getName() + ".authorities", jcacheConfiguration);
          
            cm.createCache(com.m2i.omnicanal.application.domain.UniteOrg.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.application.domain.TypeUniteOrg.class.getName(), jcacheConfiguration);
            // jhipster-needle-ehcache-add-entry
            
            cm.createCache(com.m2i.omnicanal.model.ServiceTraitement.class.getName(), jcacheConfiguration);      
            
            cm.createCache(com.m2i.omnicanal.model.Methode.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.FournisseurService.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Api.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Parametre.class.getName(), jcacheConfiguration);    
            cm.createCache(com.m2i.omnicanal.model.ParametreUtilisateur.class.getName(), jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.InterfaceUtilisateur.class.getName(), jcacheConfiguration);
           
            cm.createCache(com.m2i.omnicanal.model.Api.class.getName() + ".fournisseurService", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.Api.class.getName() + ".constitues", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.FournisseurService.class.getName() + ".fournits", jcacheConfiguration);
           
            cm.createCache(com.m2i.omnicanal.model.InterfaceUtilisateur.class.getName() + ".constitues", jcacheConfiguration);
            cm.createCache(com.m2i.omnicanal.model.FournisseurService.class.getName() + ".realisepars", jcacheConfiguration);
        };
    }
}
