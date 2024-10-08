package com.bureauworks.translator_document_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bureauworks.translator_document_management.entity.Document;
import com.bureauworks.translator_document_management.repository.DocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.List;
import java.util.Set;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private OpenAIService openAIService;

    public Page<Document> findAll(Pageable pageable) {
        return documentRepository.findAllByOrderByCreateAtDesc(pageable);
    }

    public List<Document> findAllByTranslatorId(Long id) {
        return documentRepository.findAllByTranslatorId(id);
    }

    public Page<Document> searchDocuments(String text, Pageable pageable) {
        return documentRepository.searchByText(text, pageable);
    }

    public Document save(Document document) {
        validateDocument(document);
        detectAndSetLanguage(document);
        return documentRepository.save(document);
    }

    public void validateDocument(Document document) {
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Document> violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public void detectAndSetLanguage(Document document) {
        if (document.getLocation() == null || document.getLocation().isEmpty()) {
            String content = document.getContent();
            String location = openAIService.detectLanguage(content.substring(0, Math.min(content.length(), 40)));
            document.setLocation(location);
        }
    }

    public Document findById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        documentRepository.deleteById(id);
    }
}