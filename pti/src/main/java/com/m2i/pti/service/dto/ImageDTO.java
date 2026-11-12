package com.m2i.pti.service.dto;

public class ImageDTO {

    public Long documentId;
    public byte[] imageFinal;
    public String valeurContentType;
    public String imageLink;

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public byte[] getImageFinal() {
        return imageFinal;
    }

    public void setImageFinal(byte[] imageFinal) {
        this.imageFinal = imageFinal;
    }

    public String getValeurContentType() {
        return valeurContentType;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
