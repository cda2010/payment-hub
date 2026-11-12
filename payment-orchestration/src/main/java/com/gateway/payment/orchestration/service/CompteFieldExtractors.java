package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.domain.CompteFse;

import java.util.List;

/**
 * Extracteurs natifs alignés Omnicanal ({@code getNumeroCompte}, {@code getCodeBanque}, …).
 * Pour en ajouter un sans toucher ce fichier : bean {@link CompteFieldExtractor}.
 */
public final class CompteFieldExtractors {

    private CompteFieldExtractors() {}

    public static final CompteFieldExtractor TEL_QUEL = new CompteFieldExtractor() {
        @Override
        public String code() {
            return "TEL_QUEL";
        }

        @Override
        public boolean passthrough() {
            return true;
        }

        @Override
        public String extract(CompteFse compte) {
            return compte != null ? compte.getNumeroCompte() : null;
        }
    };

    public static final CompteFieldExtractor NUMERO_COMPTE = simple("NUMERO_COMPTE", CompteFse::getNumeroCompte);
    public static final CompteFieldExtractor CODE_BANQUE = simple("CODE_BANQUE", CompteFse::getCodeBanque);
    public static final CompteFieldExtractor CODE_AGENCE = simple("CODE_AGENCE", CompteFse::getCodeAgence);
    public static final CompteFieldExtractor CLE = simple("CLE", CompteFse::getCle);
    public static final CompteFieldExtractor DEVISE = simple("DEVISE", CompteFse::getDevise);
    public static final CompteFieldExtractor FSE_TENEUR = simple("FSE_TENEUR", CompteFse::getFseTeneur);
    public static final CompteFieldExtractor FSE_TITULAIRE = simple("FSE_TITULAIRE", CompteFse::getFseTitulaire);
    public static final CompteFieldExtractor LIBELLE_COMPTE = simple("LIBELLE_COMPTE", CompteFse::getLibelleCompte);
    public static final CompteFieldExtractor CODE_UO_TITULAIRE = simple("CODE_UO_TITULAIRE", CompteFse::getCodeUoTitulaire);
    public static final CompteFieldExtractor AGENCE_CODE = simple("AGENCE_CODE", CompteFse::getAgenceCode);

    public static final CompteFieldExtractor TYPE_COMPTE = new CompteFieldExtractor() {
        @Override
        public String code() {
            return "TYPE_COMPTE";
        }

        @Override
        public String extract(CompteFse compte) {
            if (compte == null || compte.getTypeCompte() == null) {
                return null;
            }
            return compte.getTypeCompte().getCodeParametre();
        }
    };

    public static List<CompteFieldExtractor> builtins() {
        return List.of(
                TEL_QUEL,
                NUMERO_COMPTE,
                CODE_BANQUE,
                CODE_AGENCE,
                CLE,
                DEVISE,
                FSE_TENEUR,
                FSE_TITULAIRE,
                LIBELLE_COMPTE,
                CODE_UO_TITULAIRE,
                AGENCE_CODE,
                TYPE_COMPTE
        );
    }

    private static CompteFieldExtractor simple(String code, java.util.function.Function<CompteFse, String> getter) {
        return new CompteFieldExtractor() {
            @Override
            public String code() {
                return code;
            }

            @Override
            public String extract(CompteFse compte) {
                return compte == null ? null : getter.apply(compte);
            }
        };
    }
}
