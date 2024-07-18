package com.bureauworks.translator_document_management.service;

import com.bureauworks.translator_document_management.entity.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.bureauworks.translator_document_management.entity.Document;
import com.bureauworks.translator_document_management.repository.DocumentRepository;
import com.bureauworks.translator_document_management.repository.TranslatorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TranslatorRepository translatorRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private OpenAIService openAIService;

    @Cacheable(value = "documents", key = "#pageable")
    public Page<Document> findAll(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    //@Cacheable(value = "documents", key = "#text")
    public Page<Document> searchDocuments(String text, Pageable pageable) {
        return documentRepository.searchByText(text, pageable);
    }

    //@CachePut(value = "documents", key = "#document.id")
    public Document save(Document document) {
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Document> violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            throw new IllegalArgumentException(sb.toString());
        }
        return documentRepository.save(document);
    }

    //@Cacheable(value = "documents", key = "#id")
    public Document findById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    //@CacheEvict(value = "documents", key = "#id")
    public void deleteById(Long id) {
        documentRepository.deleteById(id);
    }

    @Async
    public CompletableFuture<List<String>> saveDocumentsFromCSV(MultipartFile file) {
        List<String> messages = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withHeader("subject", "content", "location", "author", "translator_email")
                    .withDelimiter(';')
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .withSkipHeaderRecord());

            int rowNum = 1; // Para acompanhar a linha atual, começando após o cabeçalho

            for (CSVRecord csvRecord : csvParser) {
                rowNum++;

                // Verifica se o registro é uma linha vazia ou incompleta
                if (!isRecordEmpty(csvRecord)) {
                    String subject = csvRecord.get("subject").trim();
                    String content = csvRecord.get("content").trim();
                    String location = csvRecord.get("location").trim().toLowerCase();
                    String author = csvRecord.get("author").trim();
                    String translatorEmail = csvRecord.get("translator_email").trim();

                    // Verifique se os campos obrigatórios estão presentes
                    if (subject.isEmpty() || content.isEmpty() || author.isEmpty() || translatorEmail.isEmpty()) {
                        messages.add("Linha " + rowNum + ": Campos obrigatórios estão faltando.");
                        continue;
                    }

                    if (location.isEmpty()) {
                        // Envia 40 caracteres do conteúdo para a API da OpenAI para detecção de idioma
                        location = openAIService.detectLanguage(content.substring(0, Math.min(content.length(), 40)));
                    }

                    Translator translator = translatorRepository.findByEmail(translatorEmail);
                    if (translator == null) {
                        messages.add("Linha " + rowNum + ": Tradutor não encontrado para o email: " + translatorEmail);
                        continue;
                    }

                    Document document = new Document();
                    document.setSubject(subject);
                    document.setContent(content);
                    document.setLocation(location);
                    document.setAuthor(author);
                    document.setTranslator(translator);

                    Set<ConstraintViolation<Document>> violations = validator.validate(document);
                    if (!violations.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (ConstraintViolation<Document> violation : violations) {
                            sb.append(violation.getMessage()).append("\n");
                        }
                        messages.add("Linha " + rowNum + ": " + sb.toString());
                        continue;
                    }

                    documentRepository.save(document);
                }
            }
        } catch (Exception e) {
            messages.add("Erro ao processar o arquivo CSV: " + e.getMessage());
        }

        return CompletableFuture.completedFuture(messages);
    }

    private boolean isRecordEmpty(CSVRecord record) {
        for (String field : record) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}