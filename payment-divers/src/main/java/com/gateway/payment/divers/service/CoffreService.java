package com.gateway.payment.divers.service;

import com.gateway.payment.divers.api.CoffreDtos.CoffreView;
import com.gateway.payment.divers.api.CoffreDtos.CreateCoffreRequest;
import com.gateway.payment.divers.domain.Coffre;
import com.gateway.payment.divers.repository.CoffreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CoffreService {

    private final CoffreRepository coffres;

    public CoffreService(CoffreRepository coffres) {
        this.coffres = coffres;
    }

    @Transactional(readOnly = true)
    public List<CoffreView> list(String owner) {
        return coffres.findByOwnerUsernameOrderByCreatedAtDesc(owner).stream()
                .map(CoffreService::view)
                .toList();
    }

    @Transactional(readOnly = true)
    public CoffreView get(String owner, UUID id) {
        return view(owned(owner, id));
    }

    @Transactional
    public CoffreView create(String owner, CreateCoffreRequest request) {
        String name = request.name().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Le nom du coffre est requis");
        }
        if (request.target().compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("L'objectif doit être d'au moins 1");
        }
        Coffre coffre = new Coffre();
        coffre.setOwnerUsername(owner);
        coffre.setName(name);
        coffre.setBalance(BigDecimal.ZERO);
        coffre.setTargetAmount(request.target());
        coffre.setGoal(request.goal());
        coffre.setLocked(false);
        coffre.setCreatedAt(Instant.now());
        return view(coffres.save(coffre));
    }

    @Transactional
    public CoffreView deposit(String owner, UUID id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Le versement doit être d'au moins 1");
        }
        Coffre coffre = owned(owner, id);
        if (coffre.isLocked()) {
            throw new IllegalArgumentException("Ce coffre est verrouillé");
        }
        coffre.setBalance(coffre.getBalance().add(amount));
        return view(coffres.save(coffre));
    }

    @Transactional
    public CoffreView setLocked(String owner, UUID id, boolean locked) {
        Coffre coffre = owned(owner, id);
        coffre.setLocked(locked);
        return view(coffres.save(coffre));
    }

    private Coffre owned(String owner, UUID id) {
        return coffres.findByIdAndOwnerUsername(id, owner)
                .orElseThrow(() -> new NoSuchElementException("Coffre introuvable"));
    }

    static CoffreView view(Coffre coffre) {
        return new CoffreView(
                coffre.getId(),
                coffre.getName(),
                coffre.getBalance(),
                coffre.getTargetAmount(),
                coffre.getGoal(),
                coffre.isLocked(),
                coffre.getCreatedAt()
        );
    }
}
