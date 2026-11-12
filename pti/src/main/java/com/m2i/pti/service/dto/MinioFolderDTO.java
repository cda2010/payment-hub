package com.m2i.pti.service.dto;

public class MinioFolderDTO {
    private String folder;
    private String contentType;
    private Long typeDocument;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getTypeDocument() {
        return this.typeDocument;
    }

    public void setTypeDocument(Long typeDocument) {
        this.typeDocument = typeDocument;
    }
}
