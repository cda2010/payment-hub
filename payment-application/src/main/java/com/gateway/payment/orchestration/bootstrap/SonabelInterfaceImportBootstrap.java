package com.gateway.payment.orchestration.bootstrap;

import com.gateway.payment.orchestration.service.OmnicanalInterfaceImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Importe IU + ParametreUtilisateur SONABEL (0005 / 0029) depuis l'export Omnicanal.
 */
@Component
@Order(28)
public class SonabelInterfaceImportBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SonabelInterfaceImportBootstrap.class);

    private final OmnicanalInterfaceImportService importService;

    public SonabelInterfaceImportBootstrap(OmnicanalInterfaceImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            OmnicanalInterfaceImportService.ImportResult result = importService.importSonabel();
            log.info("SONABEL Omnicanal IU importées: {} interfaces, {} paramètres — {}",
                    result.interfacesUpserted(),
                    result.parametresUpserted(),
                    result.details());
        } catch (Exception ex) {
            log.warn("Import IU SONABEL ignoré: {}", ex.getMessage());
        }
    }
}
