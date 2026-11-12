package com.gateway.payment.divers.service;

import com.gateway.payment.divers.api.CoffreDtos.CreateCoffreRequest;
import com.gateway.payment.divers.domain.Coffre;
import com.gateway.payment.divers.domain.CoffreGoal;
import com.gateway.payment.divers.repository.CoffreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoffreServiceTest {

    @Mock
    private CoffreRepository coffres;

    @InjectMocks
    private CoffreService service;

    @Test
    void createStartsEmptyAndUnlocked() {
        when(coffres.save(any(Coffre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var view = service.create("awa", new CreateCoffreRequest("Vacances", new BigDecimal("500000"), CoffreGoal.VOYAGE));

        assertThat(view.name()).isEqualTo("Vacances");
        assertThat(view.balance()).isEqualByComparingTo("0");
        assertThat(view.target()).isEqualByComparingTo("500000");
        assertThat(view.goal()).isEqualTo(CoffreGoal.VOYAGE);
        assertThat(view.locked()).isFalse();
    }

    @Test
    void depositAddsToTheBalance() {
        Coffre coffre = owned("125000");
        when(coffres.findByIdAndOwnerUsername(coffre.getId(), "awa")).thenReturn(Optional.of(coffre));
        when(coffres.save(coffre)).thenReturn(coffre);

        var view = service.deposit("awa", coffre.getId(), new BigDecimal("5000"));

        assertThat(view.balance()).isEqualByComparingTo("130000");
    }

    @Test
    void lockedCoffreRefusesADeposit() {
        Coffre coffre = owned("80000");
        coffre.setLocked(true);
        when(coffres.findByIdAndOwnerUsername(coffre.getId(), "awa")).thenReturn(Optional.of(coffre));

        assertThatThrownBy(() -> service.deposit("awa", coffre.getId(), new BigDecimal("5000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ce coffre est verrouillé");
    }

    @Test
    void anotherOwnerDoesNotSeeTheCoffre() {
        UUID id = UUID.randomUUID();
        when(coffres.findByIdAndOwnerUsername(id, "jean")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("jean", id))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void lockPersistsTheFlag() {
        Coffre coffre = owned("1000");
        when(coffres.findByIdAndOwnerUsername(coffre.getId(), "awa")).thenReturn(Optional.of(coffre));
        when(coffres.save(coffre)).thenReturn(coffre);

        var view = service.setLocked("awa", coffre.getId(), true);

        ArgumentCaptor<Coffre> saved = ArgumentCaptor.forClass(Coffre.class);
        verify(coffres).save(saved.capture());
        assertThat(saved.getValue().isLocked()).isTrue();
        assertThat(view.locked()).isTrue();
    }

    private static Coffre owned(String balance) {
        Coffre coffre = new Coffre();
        coffre.setId(UUID.randomUUID());
        coffre.setOwnerUsername("awa");
        coffre.setName("Fonds");
        coffre.setBalance(new BigDecimal(balance));
        coffre.setTargetAmount(new BigDecimal("200000"));
        coffre.setGoal(CoffreGoal.URGENCE);
        coffre.setLocked(false);
        coffre.setCreatedAt(Instant.parse("2026-11-09T00:00:00Z"));
        return coffre;
    }
}
