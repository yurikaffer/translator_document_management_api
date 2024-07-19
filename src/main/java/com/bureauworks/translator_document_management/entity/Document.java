package com.bureauworks.translator_document_management.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "document", indexes = {
        @Index(name = "idx_document_subject", columnList = "subject"),
        @Index(name = "idx_document_location", columnList = "location"),
        @Index(name = "idx_document_author", columnList = "author")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O assunto é obrigatório.")
    @Column(nullable = false)
    private String subject;

    @NotBlank(message = "O conteúdo é obrigatório.")
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String location;

    @NotBlank(message = "O autor é obrigatório.")
    @Column(nullable = false)
    private String author;

    @NotNull(message = "O tradutor é obrigatório.")
    @ManyToOne
    @JoinColumn(name = "translator_id", nullable = false)
    private Translator translator;

    @ManyToOne
    @JoinColumn(name = "document_import_id", nullable = true)
    @JsonBackReference
    private DocumentImport documentImport;

    public Document() {
    }

    public Document(String subject, String content, String location, String author, Translator translator) {
        this.subject = subject;
        this.content = content;
        this.location = location;
        this.author = author;
        this.translator = translator;
    }

    public Document(String subject, String content, String location, String author, Translator translator, DocumentImport documentImport) {
        this.subject = subject;
        this.content = content;
        this.location = location;
        this.author = author;
        this.translator = translator;
        this.documentImport = documentImport;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Translator getTranslator() {
        return translator;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    public DocumentImport getDocumentImport() {
        return documentImport;
    }

    public void setDocumentImport(DocumentImport documentImport) {
        this.documentImport = documentImport;
    }
}