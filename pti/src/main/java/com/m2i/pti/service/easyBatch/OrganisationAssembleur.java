package com.m2i.pti.service.easyBatch;

public class OrganisationAssembleur implements CsvAssembler<CsvOrganisation>{
    @Override
    public CsvOrganisation assemble(String[] cols) {
        CsvOrganisation csvOrganisation=new CsvOrganisation();
        csvOrganisation.setNom(cols[0]);
        csvOrganisation.setTelephone(cols[1]);
        return csvOrganisation;
    }
}
