package com.gateway.payment.divers.service;

import com.gateway.payment.divers.api.TontineDtos.CreateTontineRequest;
import com.gateway.payment.divers.api.TontineDtos.MemberView;
import com.gateway.payment.divers.api.TontineDtos.TontineView;
import com.gateway.payment.divers.domain.Tontine;
import com.gateway.payment.divers.domain.TontineFrequency;
import com.gateway.payment.divers.domain.TontineMember;
import com.gateway.payment.divers.domain.TontineStatus;
import com.gateway.payment.divers.repository.TontineMemberRepository;
import com.gateway.payment.divers.repository.TontineRepository;
import com.gateway.payment.inbox.kafka.PartyActionPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TontineService {

    private static final Logger log = LoggerFactory.getLogger(TontineService.class);
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TontineRepository tontines;
    private final TontineMemberRepository members;
    private final PartyActionPublisher actions;

    public TontineService(
            TontineRepository tontines,
            TontineMemberRepository members,
            PartyActionPublisher actions
    ) {
        this.tontines = tontines;
        this.members = members;
        this.actions = actions;
    }

    @Transactional(readOnly = true)
    public List<TontineView> list(String username) {
        return members.findByUsernameOrderByTontine_CreatedAtDesc(username).stream()
                .map(membership -> view(membership.getTontine(), username))
                .toList();
    }

    @Transactional(readOnly = true)
    public TontineView get(String username, UUID id) {
        Tontine tontine = requireMember(username, id).getTontine();
        return view(tontine, username);
    }

    @Transactional
    public TontineView create(String username, UUID partyId, CreateTontineRequest request) {
        String name = request.name().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tontine est requis");
        }
        if (request.contribution().compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("La cotisation doit être d'au moins 1");
        }
        Tontine tontine = new Tontine();
        tontine.setName(name);
        tontine.setContribution(request.contribution());
        tontine.setFrequency(request.frequency());
        tontine.setRoundNo(1);
        tontine.setInviteCode(nextInviteCode());
        tontine.setStatus(TontineStatus.ACTIVE);
        tontine.setCreatedAt(Instant.now());
        tontine = tontines.save(tontine);
        members.save(member(tontine, username, partyId, 1, true));
        cotisationDue(tontine, partyId);
        return view(tontine, username);
    }

    @Transactional
    public TontineView join(String username, UUID partyId, String inviteCode) {
        String code = inviteCode == null ? "" : inviteCode.trim();
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Le code d'invitation est requis");
        }
        Tontine tontine = tontines.findByInviteCodeIgnoreCase(code)
                .orElseThrow(() -> new NoSuchElementException("Tontine introuvable"));
        if (tontine.getStatus() == TontineStatus.COMPLETED) {
            throw new IllegalArgumentException("Cette tontine est terminée");
        }
        if (members.existsByTontineIdAndUsername(tontine.getId(), username)) {
            throw new IllegalArgumentException("Vous participez déjà à cette tontine");
        }
        int turn = members.countByTontineId(tontine.getId()) + 1;
        members.save(member(tontine, username, partyId, turn, false));
        cotisationDue(tontine, partyId);
        return view(tontine, username);
    }

    /**
     * Enregistre la cotisation du tour en cours. Quand chaque membre a payé, le tour avance.
     * Le dernier tour clôt la tontine.
     */
    @Transactional
    public TontineView contribute(String username, UUID partyId, UUID id) {
        TontineMember mine = requireMember(username, id);
        Tontine tontine = mine.getTontine();
        if (tontine.getStatus() == TontineStatus.COMPLETED) {
            throw new IllegalArgumentException("Cette tontine est terminée");
        }
        if (mine.isPaid()) {
            throw new IllegalArgumentException("Cotisation déjà enregistrée pour ce tour");
        }
        if (partyId != null) {
            mine.setPartyId(partyId);
        }
        mine.setPaid(true);
        members.save(mine);
        List<TontineMember> roster = members.findByTontineIdOrderByTurnNoAsc(tontine.getId());
        rememberParty(roster, username, partyId);
        boolean everyonePaid = roster.stream().allMatch(TontineMember::isPaid);
        if (everyonePaid) {
            if (tontine.getRoundNo() >= roster.size()) {
                tontine.setStatus(TontineStatus.COMPLETED);
                roster.forEach(member -> cotisationPaid(member.getPartyId(), tontine.getId()));
            } else {
                tontine.setRoundNo(tontine.getRoundNo() + 1);
                roster.forEach(member -> member.setPaid(false));
                members.saveAll(roster);
                roster.forEach(member -> cotisationDue(tontine, member.getPartyId()));
            }
            tontines.save(tontine);
        } else {
            cotisationPaid(partyId != null ? partyId : mine.getPartyId(), tontine.getId());
        }
        return view(tontine, username);
    }

    private TontineMember requireMember(String username, UUID tontineId) {
        return members.findByTontineIdAndUsername(tontineId, username)
                .orElseThrow(() -> new NoSuchElementException("Tontine introuvable"));
    }

    private String nextInviteCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            StringBuilder code = new StringBuilder("TONT-");
            for (int i = 0; i < 4; i++) {
                code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            if (!tontines.existsByInviteCode(code.toString())) {
                return code.toString();
            }
        }
        throw new IllegalStateException("Impossible de générer un code d'invitation");
    }

    private static TontineMember member(
            Tontine tontine, String username, UUID partyId, int turn, boolean admin
    ) {
        TontineMember member = new TontineMember();
        member.setTontine(tontine);
        member.setUsername(username);
        member.setPartyId(partyId);
        member.setDisplayName(username);
        member.setTurnNo(turn);
        member.setPaid(false);
        member.setAdminMember(admin);
        return member;
    }

    private static void rememberParty(List<TontineMember> roster, String username, UUID partyId) {
        if (partyId == null) {
            return;
        }
        for (TontineMember member : roster) {
            if (username.equals(member.getUsername())) {
                member.setPartyId(partyId);
            }
        }
    }

    private void cotisationDue(Tontine tontine, UUID partyId) {
        if (partyId == null) {
            return;
        }
        try {
            actions.open(
                    partyId,
                    "COTISATION_DUE",
                    "tontine",
                    tontine.getId().toString(),
                    tontine.getName(),
                    dueAt(tontine));
        } catch (Exception e) {
            log.warn("Échec publication COTISATION_DUE pour {} : {}", partyId, e.getMessage());
        }
    }

    private void cotisationPaid(UUID partyId, UUID tontineId) {
        if (partyId == null) {
            return;
        }
        try {
            actions.complete(partyId, "COTISATION_DUE", "tontine", tontineId.toString());
        } catch (Exception e) {
            log.warn("Échec publication COTISATION_DUE close pour {} : {}", partyId, e.getMessage());
        }
    }

    private static Instant dueAt(Tontine tontine) {
        long days = tontine.getFrequency() == TontineFrequency.HEBDOMADAIRE ? 7 : 30;
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    private TontineView view(Tontine tontine, String username) {
        List<TontineMember> roster = members.findByTontineIdOrderByTurnNoAsc(tontine.getId());
        TontineMember mine = roster.stream()
                .filter(member -> member.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Tontine introuvable"));
        List<MemberView> memberViews = roster.stream()
                .map(member -> new MemberView(
                        member.getId(),
                        member.getDisplayName(),
                        initials(member.getDisplayName()),
                        member.isPaid(),
                        member.getTurnNo(),
                        member.getUsername().equals(username),
                        member.isAdminMember() ? "admin" : "member"
                ))
                .toList();
        int nextIndex = 0;
        for (int i = 0; i < roster.size(); i++) {
            if (roster.get(i).getTurnNo() == tontine.getRoundNo()) {
                nextIndex = i;
                break;
            }
        }
        return new TontineView(
                tontine.getId(),
                tontine.getName(),
                tontine.getContribution(),
                tontine.getFrequency(),
                tontine.getRoundNo(),
                roster.size(),
                tontine.getInviteCode(),
                nextIndex,
                mine.isPaid(),
                mine.isAdminMember() ? "admin" : "member",
                tontine.getStatus(),
                tontine.getCreatedAt(),
                memberViews
        );
    }

    static String initials(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}
