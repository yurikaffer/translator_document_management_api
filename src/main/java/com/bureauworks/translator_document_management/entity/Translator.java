package com.bureauworks.translator_document_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "translator", indexes = {
        @Index(name = "idx_translator_name", columnList = "name"),
        @Index(name = "idx_translator_email", columnList = "email")
})
public class Translator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório.")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "O email é obrigatório.")
    @Email(message = "O email deve ser válido.")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "O idioma de origem é obrigatório.")
    @Column(nullable = false)
    private String sourceLanguage;

    @NotBlank(message = "O idioma de destino é obrigatório.")
    @Column(nullable = false)
    private String targetLanguage;

    @OneToMany(mappedBy = "translator", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Document> documents;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
