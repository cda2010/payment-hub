package com.m2i.pti.service.easyBatch;

public class FactureAssembler implements CsvAssembler<CSVFacture> {
    @Override
    public CSVFacture assemble(String[] cols) {
        CSVFacture csvFacture = new CSVFacture();
        csvFacture.setRefAbonne(cols[0]);
        csvFacture.setTypePiece(cols[1]);
        csvFacture.setNumeroPiece(cols[2]);
        csvFacture.setSoldePiece(cols[3]);
        csvFacture.setDateLimitePiece(cols[4]);
        csvFacture.setDateAppliPenalite(cols[5]);
        csvFacture.setDateLettrage(cols[6]);
        csvFacture.setCategorieComAbonne(cols[7]);
        csvFacture.setNomAbonne(cols[8]);
        csvFacture.setAnneFacture(cols[9]);
        csvFacture.setMoisFacture(cols[10]);
        return csvFacture;
    }
}
