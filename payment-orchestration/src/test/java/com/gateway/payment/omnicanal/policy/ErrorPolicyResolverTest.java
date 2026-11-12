package com.gateway.payment.omnicanal.policy;

import com.gateway.payment.orchestration.domain.StepErrorPolicy;
import com.gateway.payment.orchestration.repository.StepErrorPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorPolicyResolverTest {

    private InMemoryStepErrorPolicyRepository repo;
    private ErrorPolicyResolver resolver;

    @BeforeEach
    void setUp() {
        repo = new InMemoryStepErrorPolicyRepository();
        resolver = new ErrorPolicyResolver(repo);
    }

    @Test
    void timeoutOnCriticalStep_triggersCompensation() {
        ErrorDecision decision = resolver.resolve(ErrorResolveRequest.builder()
                .stepKey("transfertDest")
                .message("Timeout / indisponibilité fournisseur")
                .criticality(StepCriticality.CRITICAL)
                .workflowPolicy(WorkflowErrorPolicy.globalDefault())
                .build());

        assertEquals(ErrorAction.COMPENSATE, decision.actionNormalized());
        assertTrue(decision.compensate());
        assertEquals(ErrorType.TIMEOUT, decision.errorType());
    }

    @Test
    void technicalOnCriticalStep_triggersCompensation() {
        ErrorDecision decision = resolver.resolve(ErrorResolveRequest.builder()
                .stepKey("transfertDest")
                .message("Impossible d'obtenir un token OAuth: I/O error")
                .criticality(StepCriticality.CRITICAL)
                .workflowPolicy(WorkflowErrorPolicy.globalDefault())
                .build());

        assertEquals(ErrorAction.COMPENSATE, decision.actionNormalized());
        assertTrue(decision.compensate());
        assertEquals(ErrorType.TRANSIENT, decision.errorType());
    }

    @Test
    void blankDefaultErrorAction_doesNotForceStopWorkflow() {
        ErrorDecision decision = resolver.resolve(ErrorResolveRequest.builder()
                .stepKey("transfertDest")
                .message("Timeout / indisponibilité fournisseur")
                .criticality(StepCriticality.CRITICAL)
                .stepDefaultAction(ErrorAction.parseOptional(null))
                .workflowPolicy(WorkflowErrorPolicy.globalDefault())
                .build());

        assertEquals(ErrorAction.COMPENSATE, decision.actionNormalized());
    }

    @Test
    void stepPolicyFromDb_takesPrecedence() {
        repo.add(StepErrorPolicy.builder()
                .id(UUID.randomUUID())
                .code("CREDIT_DESTINATION")
                .errorType("BUSINESS_REJECTED")
                .action("COMPENSATE")
                .active(true)
                .build());

        ErrorDecision decision = resolver.resolve(ErrorResolveRequest.builder()
                .stepKey("transfertDest")
                .message("Compte inconnu")
                .orchErrorCode("BUSINESS_REJECTED")
                .criticality(StepCriticality.CRITICAL)
                .stepErrorPolicyCode("CREDIT_DESTINATION")
                .workflowPolicy(WorkflowErrorPolicy.globalDefault())
                .build());

        assertEquals(ErrorAction.COMPENSATE, decision.actionNormalized());
        assertEquals("STEP", decision.ruleSource());
    }

    /** Stub minimal — évite Mockito (self-attach sandbox). */
    static final class InMemoryStepErrorPolicyRepository implements StepErrorPolicyRepository {
        private final List<StepErrorPolicy> rows = new ArrayList<>();

        void add(StepErrorPolicy p) {
            rows.add(p);
        }

        @Override
        public Optional<StepErrorPolicy> findActiveStepRule(String code, String errorType) {
            return rows.stream()
                    .filter(p -> p.isActive() && p.getSchemaEcritureId() == null
                            && code.equals(p.getCode()) && errorType.equals(p.getErrorType()))
                    .findFirst();
        }

        @Override
        public Optional<StepErrorPolicy> findActiveSchemaRule(String code, String errorType, UUID schemaEcritureId) {
            return rows.stream()
                    .filter(p -> p.isActive() && schemaEcritureId.equals(p.getSchemaEcritureId())
                            && code.equals(p.getCode()) && errorType.equals(p.getErrorType()))
                    .findFirst();
        }

        @Override
        public void flush() {}

        @Override
        public <S extends StepErrorPolicy> S saveAndFlush(S entity) {
            return save(entity);
        }

        @Override
        public <S extends StepErrorPolicy> List<S> saveAllAndFlush(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public void deleteAllInBatch(Iterable<StepErrorPolicy> entities) {}

        @Override
        public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}

        @Override
        public void deleteAllInBatch() {}

        @Override
        public StepErrorPolicy getOne(UUID uuid) {
            return findById(uuid).orElseThrow();
        }

        @Override
        public StepErrorPolicy getById(UUID uuid) {
            return findById(uuid).orElseThrow();
        }

        @Override
        public StepErrorPolicy getReferenceById(UUID uuid) {
            return findById(uuid).orElseThrow();
        }

        @Override
        public <S extends StepErrorPolicy> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends StepErrorPolicy> List<S> findAll(Example<S> example) {
            return List.of();
        }

        @Override
        public <S extends StepErrorPolicy> List<S> findAll(Example<S> example, Sort sort) {
            return List.of();
        }

        @Override
        public <S extends StepErrorPolicy> Page<S> findAll(Example<S> example, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public <S extends StepErrorPolicy> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends StepErrorPolicy> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends StepErrorPolicy, R> R findBy(
                Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public <S extends StepErrorPolicy> S save(S entity) {
            rows.add(entity);
            return entity;
        }

        @Override
        public <S extends StepErrorPolicy> List<S> saveAll(Iterable<S> entities) {
            List<S> out = new ArrayList<>();
            entities.forEach(e -> out.add(save(e)));
            return out;
        }

        @Override
        public Optional<StepErrorPolicy> findById(UUID uuid) {
            return rows.stream().filter(p -> uuid.equals(p.getId())).findFirst();
        }

        @Override
        public boolean existsById(UUID uuid) {
            return findById(uuid).isPresent();
        }

        @Override
        public List<StepErrorPolicy> findAll() {
            return List.copyOf(rows);
        }

        @Override
        public List<StepErrorPolicy> findAllById(Iterable<UUID> uuids) {
            return List.of();
        }

        @Override
        public long count() {
            return rows.size();
        }

        @Override
        public void deleteById(UUID uuid) {}

        @Override
        public void delete(StepErrorPolicy entity) {}

        @Override
        public void deleteAllById(Iterable<? extends UUID> uuids) {}

        @Override
        public void deleteAll(Iterable<? extends StepErrorPolicy> entities) {}

        @Override
        public void deleteAll() {
            rows.clear();
        }

        @Override
        public List<StepErrorPolicy> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<StepErrorPolicy> findAll(Pageable pageable) {
            return Page.empty();
        }
    }
}
