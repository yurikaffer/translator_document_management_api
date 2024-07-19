package com.bureauworks.translator_document_management.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "import_error")
public class ImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @ManyToOne
    @JoinColumn(name = "document_import_id", nullable = false)
    @JsonBackReference
    private DocumentImport documentImport;

    public ImportError() {}

    public ImportError(String message, DocumentImport documentImport) {
        this.message = message;
        this.documentImport = documentImport;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DocumentImport getDocumentImport() {
        return documentImport;
    }

    public void setDocumentImport(DocumentImport documentImport) {
        this.documentImport = documentImport;
    }
}
