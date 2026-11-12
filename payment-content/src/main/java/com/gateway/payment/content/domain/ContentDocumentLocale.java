package com.gateway.payment.content.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "content_document_locale")
@Getter
@Setter
public class ContentDocumentLocale {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private ContentDocument document;

    @Column(nullable = false, length = 8)
    private String locale;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 512)
    private String summary;

    @Column(columnDefinition = "text")
    private String body;
}
