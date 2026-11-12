package com.m2i.pti.service.easyBatch;



public class CompteAssembleur implements CsvAssembler<CsvCompte> {
    @Override
    public CsvCompte assemble (String[] cols){
        CsvCompte csvCompte = new CsvCompte();
        csvCompte.setNom(cols[0]);
        csvCompte.setPrenom(cols[1]);
        csvCompte.setTelephone(cols[2]);
        csvCompte.setDateNaissance(cols[3]);
        csvCompte.setLieuNaissance(cols[4]);
        csvCompte.setNomMere(cols[5]);
        csvCompte.setPersonnePrevenir(cols[6]);
        csvCompte.setEmail(cols[7]);
        csvCompte.setAdresse(cols[8]);
        csvCompte.setTypePersonne(cols[9]);
        csvCompte.setSexe(cols[10]);
        csvCompte.setCodeTypePiece(cols[11]);
        csvCompte.setReference(cols[12]);
        csvCompte.setDateDelivrance(cols[13]);
            csvCompte.setDateExpiration(cols[14]);
        csvCompte.setLieuDelivrance(cols[15]);
         csvCompte.setCodePays(Integer.parseInt(cols[16]));
        return csvCompte;
    }
}
