package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.api.dto.LegCompileRequest;
import com.gateway.payment.orchestration.domain.CompteFse;
import com.gateway.payment.orchestration.domain.ParametreCategorie;
import com.gateway.payment.orchestration.domain.ParametreSchema;
import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.domain.RelationFse;
import com.gateway.payment.orchestration.domain.TypeCompte;
import com.gateway.payment.orchestration.repository.CompteFseRepository;
import com.gateway.payment.orchestration.repository.LigneSchemaEcritureRepository;
import com.gateway.payment.orchestration.repository.RelationFseRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegCompilerSchemaCompteResolutionTest {

    private final UUID relationId = UUID.fromString("d0000001-0000-4000-8000-000000000001");

    @Test
    void applySchemaBindings_resolvesCpteloroFromCompteCatalogue() {
        RelationFse relation = relation();
        LegCompilerService service = newService(
                relation,
                Map.of("CPTELORO|0011", compte("000000279", "CPTELORO", "0011", relation))
        );

        Map<String, String> resolved = new HashMap<>(Map.of(
                "CPTSOURCE", "0011000003090",
                "MONTANT", "1000"
        ));
        LegCompileRequest request = new LegCompileRequest(
                "0011", "0004", "TRANSFERT", ProprieteApi.FSESOURCE,
                Map.of("CPTSOURCE", "0011000003090", "CPTDEST", "BF0600100000111100001", "MONTANT", "1000"),
                null
        );

        service.applySchemaBindings(
                resolved,
                new HashMap<>(request.userParams()),
                List.of(
                        binding("CPTSOURCE", "CPTSOURCE", ParametreCategorie.USER),
                        binding("CPTELORO", "CPTDEST", ParametreCategorie.SYSTEM),
                        binding("MONTANT", "MONTANT", ParametreCategorie.USER)
                ),
                request,
                "0011",
                ServiceTypeCode.TRANSFERT,
                Map.of(
                        "CPTSOURCE", CompteFieldExtractors.TEL_QUEL,
                        "CPTDEST", CompteFieldExtractors.NUMERO_COMPTE
                )
        );
        LegCompilerService.propagateInterfaceAliases(
                List.of(
                        ParametreUtilisateur.builder()
                                .codeParametre("compteDebiteur")
                                .codeParametreMethode("CPTSOURCE")
                                .build(),
                        ParametreUtilisateur.builder()
                                .codeParametre("compteCredit")
                                .codeParametreMethode("CPTDEST")
                                .build()
                ),
                resolved
        );

        assertEquals("0011000003090", resolved.get("CPTSOURCE"));
        assertEquals("000000279", resolved.get("CPTDEST"));
        assertEquals("000000279", resolved.get("compteCredit"));
    }

    @Test
    void applySchemaBindings_usesFseTeneurExtractor() {
        RelationFse relation = relation();
        LegCompilerService service = newService(
                relation,
                Map.of("CPTELORO|0011", compte("000000279", "CPTELORO", "0011", relation))
        );

        Map<String, String> resolved = new HashMap<>();
        LegCompileRequest request = new LegCompileRequest(
                "0011", "0004", "TRANSFERT", ProprieteApi.FSESOURCE,
                Map.of("CPTSOURCE", "0011000003090"),
                null
        );

        service.applySchemaBindings(
                resolved,
                new HashMap<>(request.userParams()),
                List.of(binding("CPTELORO", "CPTDEST", ParametreCategorie.SYSTEM)),
                request,
                "0011",
                ServiceTypeCode.TRANSFERT,
                Map.of("CPTDEST", CompteFieldExtractors.FSE_TENEUR)
        );

        assertEquals("0011", resolved.get("CPTDEST"));
    }

    @Test
    void extractorsByMethodRole_parsesAttributFromInterface() {
        LegCompilerService service = newService(relation(), Map.of());
        Map<String, CompteFieldExtractor> extractors = service.extractorsByMethodRole(List.of(
                ParametreUtilisateur.builder()
                        .codeParametre("compteCredit")
                        .codeParametreMethode("CPTDEST")
                        .attribut("getNumeroCompte")
                        .build()
        ));
        assertEquals(CompteFieldExtractors.NUMERO_COMPTE, extractors.get("CPTDEST"));
        assertEquals(CompteFieldExtractors.NUMERO_COMPTE, extractors.get("compteCredit"));
    }

    @Test
    void applySchemaBindings_resolvesCptemiroirOnDestLeg() {
        RelationFse relation = relation();
        LegCompilerService service = newService(
                relation,
                Map.of("CPTEMIROIR|0004", compte("00047324101", "CPTEMIROIR", "0004", relation))
        );

        Map<String, String> resolved = new HashMap<>(Map.of(
                "CPTDEST", "BF0600100000111100001",
                "MONTANT", "1000"
        ));
        LegCompileRequest request = new LegCompileRequest(
                "0011", "0004", "TRANSFERT", ProprieteApi.FSEDESTINATAIRE,
                Map.of("CPTSOURCE", "0011000003090", "CPTDEST", "BF0600100000111100001"),
                null
        );

        service.applySchemaBindings(
                resolved,
                new HashMap<>(request.userParams()),
                List.of(
                        binding("CPTEMIROIR", "CPTSOURCE", ParametreCategorie.SYSTEM),
                        binding("CPTDEST", "CPTDEST", ParametreCategorie.USER)
                ),
                request,
                "0004",
                ServiceTypeCode.TRANSFERT
        );
        LegCompilerService.propagateInterfaceAliases(
                List.of(
                        ParametreUtilisateur.builder()
                                .codeParametre("compteDebiteur")
                                .codeParametreMethode("CPTSOURCE")
                                .build(),
                        ParametreUtilisateur.builder()
                                .codeParametre("compteCredit")
                                .codeParametreMethode("CPTDEST")
                                .build()
                ),
                resolved
        );

        assertEquals("00047324101", resolved.get("CPTSOURCE"));
        assertEquals("00047324101", resolved.get("compteDebiteur"));
        assertEquals("BF0600100000111100001", resolved.get("CPTDEST"));
    }

    private LegCompilerService newService(RelationFse relation, Map<String, CompteFse> comptes) {
        RelationFseRepository relationRepo = stub(RelationFseRepository.class, (method, args) -> {
            if ("findRelation".equals(method.getName())) {
                return Optional.of(relation);
            }
            return defaultReturn(method.getReturnType());
        });
        CompteFseRepository compteRepo = stub(CompteFseRepository.class, (method, args) -> {
            if ("findCompteFse".equals(method.getName())) {
                String etiquette = (String) args[0];
                String fseTeneur = (String) args[3];
                return Optional.ofNullable(comptes.get(etiquette + "|" + fseTeneur));
            }
            return defaultReturn(method.getReturnType());
        });
        return new LegCompilerService(
                null,
                stub(LigneSchemaEcritureRepository.class, (m, a) -> defaultReturn(m.getReturnType())),
                relationRepo,
                compteRepo,
                CompteFieldExtractorRegistry.defaults(),
                new ParametrePayloadExtractor()
        );
    }

    private RelationFse relation() {
        return RelationFse.builder()
                .id(relationId)
                .fseSource("0011")
                .fseDestination("0004")
                .serviceType(ServiceTypeCode.TRANSFERT)
                .build();
    }

    private static CompteFse compte(String numero, String etiquette, String fseTeneur, RelationFse relation) {
        return CompteFse.builder()
                .numeroCompte(numero)
                .typeCompte(TypeCompte.builder().codeParametre(etiquette).libelle(etiquette).build())
                .fseTeneur(fseTeneur)
                .fseTitulaire(fseTeneur)
                .codeBanque("00000")
                .codeAgence("")
                .extype("CompteFse")
                .libelleCompte("")
                .agenceCode("01001")
                .relation(relation)
                .serviceType(ServiceTypeCode.TRANSFERT)
                .build();
    }

    private static ParametreSchema binding(String code, String method, ParametreCategorie cat) {
        return ParametreSchema.builder()
                .codeParametre(code)
                .codeParametreMethode(method)
                .categorie(cat)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type, Invoker invoker) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> "stub:" + type.getSimpleName();
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }
                    return invoker.invoke(method, args);
                }
        );
    }

    private static Object defaultReturn(Class<?> returnType) {
        if (returnType == Optional.class) {
            return Optional.empty();
        }
        if (returnType == List.class) {
            return List.of();
        }
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0L;
        }
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        return null;
    }

    @FunctionalInterface
    private interface Invoker {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
