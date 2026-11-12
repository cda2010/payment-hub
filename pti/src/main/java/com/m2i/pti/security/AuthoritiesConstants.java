package com.m2i.pti.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {
     public static final String DEVELOPPER = "ROLE_DEV";

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String CONSULTER_OPERATION = "ROLE_CONSULT_OPERATION";

    public static final String OPERATION_APPROVISIONNEMENT = "ROLE_DO_OP_APPROV";

    public static final String OPERATION_ENVOI_CASH = "ROLE_DO_OP_ENVOI_CASH";

    public static final String OPERATION_RETRAIT = "ROLE_DO_OP_RETRAIT";

    public static final String OPERATION_CASH_ACCOUNT = "ROLE_DO_OP_CASH_ACCOUNT";

    public static final String OPERATION_MAD_INITIATION = "ROLE_DO_OP_MAD_INITIATION";

    public static final String OPERATION_MAD_VALIDATION = "ROLE_DO_OP_MAD_VALIDATION";

    public static final String OPERATION_VIREMENT_INITIATION = "ROLE_DO_OP_VIREMENT_INITIATION";

    public static final String OPERATION_VIREMENT_VALIDATION = "ROLE_DO_OP_VIREMENT_VALIDATION";

    public static final String DO_VALIDATION = "ROLE_OP_DO_VALIDATION";

    public static final String CREATE_COMPTE = "ROLE_CRE_COMPTE";

    public static final String CONSULT_ALL_TRANSACTION = "ROLE_CONSULT_ALL_TRANSACTIONS";

    public static final String CONSULT_ORGANISATION = "ROLE_CONSULT_ORG";

    public static final String SELECT_USER_PV = "ROLE_SELECT_USER_PV";

    public static final String CONSULT_COMPT_TRANSACTIONS = "ROLE_CONSULT_COMPT_TRANSACTIONS";

    public static final String API_AGENT="ROLE_CALL_API_AGENT";
    public static final String API_DEV="ROLE_CALL_API_DEV";

    /*
     *  ROLE ADMIN SYSTEME
     **/
    public static final String ADMIN_SYSTEME = "ROLE_ADMIN_SYSTEME";//Admin general de la plateform
    public static final String CREATE_USER = "ROLE_CRE_USER";//Role permettant d'ajouter un nouveau utilisateur
    public static final String VIEW_ALL_USER = "ROLE_ALL_USER";//Role permettant de visualiser la liste des utilisateurs
    public static final String UPDATE_USER = "ROLE_UPDATE_USER";//Role permettant de modifier un utilisateur

    public static final String CREATE_ORGANISATION = "ROLE_CRE_ORG";//Role permettant de creer une organisation et tout ce qui va avec
    public static final String CREATE_PROFIL = "ROLE_CRE_PROFIL";// Role permettant de creer un profil
    public static final String CREATE_TYPE_SERVICE = "ROLE_CRE_TYPE_SERVICE";
    public static final String CREATE_TYPE_COMPTE = "ROLE_CRE_TYPE_COMPTE";
    public static final String CREATE_CANAL = "ROLE_CRE_CANAL";
    public static final String CREATE_DEVISE = "ROLE_CRE_DEVISE";
    public static final String CREATE_NATIONALITE = "ROLE_CRE_NATIONALITE";
    public static final String CREATE_TYPE_ORGANISATION = "ROLE_CRE_TYPE_ORG";
    public static final String CONSULT_FOURN_SERVICE = "ROLE_CONSULT_FOURN_SERVICE";
    public static final String CREATE_FRAIS = "ROLE_CRE_FRAIS";
    public static final String CREATE_REPART_COMMISSION = "ROLE_CRE_REPART_COMMISSION";
    public static final String CONSULT_COMMISION = " ROLE_CONSULT_COMMISION";
    public static final String CREATE_REPART_FRAIS_EM_ACQ = "ROLE_CRE_REPART_FRAIS_EM_ACQ";
    public static final String CREATE_ETIQUETTE_COMPT = "ROLE_CRE_ETIQUETTE_COMPT";
    public static final String CREATE_INSTRUCTION_COMPT = "ROLE_CRE_INSTRUCTION_COMPT";
    public static final String ACTIVE_COMPTE_BANQUE = "ROLE_ACTIVE_COMPTE_BANQUE";
    public static final String CREATE_COMPTE_BANQUE = "ROLE_CREATE_COMPTE_BANQUE";
    public static final String CREATE_JOB_SERVICE = "ROLE_CRE_JOB";
    public static final String DELETE_MACHINE = "ROLE_REMOVE_MACHINE";
    public static final String    VALIDE_MESSSAGE_MASSE ="ROLE_VALIDE_MESSAGE_MASSE";
    private static final String   DO_MESSAGE_MASSE ="ROLE_MESSAGE_MASSE";
    private static final String   VOIR_DASHBOARD ="ROLE_VIEW_DASHBOARD";
    public static final String   VIEW_ALL_OPERATION ="ROLE_VIEW_ALL_OPERATION";



    private AuthoritiesConstants() {
    }
}
