package com.bureauworks.translator_document_management.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_imports", indexes = {
        @Index(name = "idx_import_fileName", columnList = "fileName")
})
public class DocumentImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "message", nullable = false)
    private String message;

    @OneToMany(mappedBy = "documentImport", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ImportError> importErrors;

    @OneToMany(mappedBy = "documentImport", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Document> documents;

    public DocumentImport() {
        this.importErrors = new ArrayList<>();
        this.documents = new ArrayList<>();
    }

    public DocumentImport(String fileName, String message) {
        this.fileName = fileName;
        this.message = message;
        this.importErrors = new ArrayList<>();
        this.documents = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ImportError> getImportErrors() {
        return importErrors;
    }

    public void setImportErrors(List<ImportError> importErrors) {
        this.importErrors = importErrors;
    }

    public void addDocument(Document document) {
        documents.add(document);
        document.setDocumentImport(this);
    }

    public void addImportError(ImportError importError) {
        importErrors.add(importError);
        importError.setDocumentImport(this);
    }
}