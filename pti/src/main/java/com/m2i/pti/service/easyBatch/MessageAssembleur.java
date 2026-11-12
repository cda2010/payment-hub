package com.m2i.pti.service.easyBatch;

public class MessageAssembleur implements CsvAssembler<CsvMessage> {
    @Override
    public CsvMessage assemble(String[] cols) {
        CsvMessage csvMessage = new CsvMessage();
        csvMessage.setCodeIndicatif(cols[0]);
        csvMessage.setNumeroTelephone(cols[1]);
        csvMessage.setEmail(cols[2]);
        csvMessage.setMessage(cols[3]);
        return csvMessage;
    }
}
