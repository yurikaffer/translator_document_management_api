package com.bureauworks.translator_document_management.service;

import com.bureauworks.translator_document_management.entity.Document;
import com.bureauworks.translator_document_management.entity.DocumentImport;
import com.bureauworks.translator_document_management.entity.ImportError;
import com.bureauworks.translator_document_management.entity.Translator;
import com.bureauworks.translator_document_management.repository.DocumentImportRepository;
import com.bureauworks.translator_document_management.repository.DocumentRepository;
import com.bureauworks.translator_document_management.repository.TranslatorRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

@Service
public class DocumentImportService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentImportService.class);

    @Autowired
    private DocumentImportRepository documentImportRepository;


    @Autowired
    private TranslatorRepository translatorRepository;


    @Autowired
    private Validator validator;

    @Autowired
    private OpenAIService openAIService;

    public Page<DocumentImport> findAll(Pageable pageable) {
        return documentImportRepository.findAll(pageable);
    }

    public Page<DocumentImport> searchImports(String text, Pageable pageable) {
        return documentImportRepository.searchByText(text, pageable);
    }

    public DocumentImport findById(Long id) {
        return documentImportRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        documentImportRepository.deleteById(id);
    }

    @Async
    public CompletableFuture<DocumentImport> saveDocumentsFromCSV(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        DocumentImport documentImport = new DocumentImport(fileName, "Importação em andamento");

        if (file.isEmpty()) {
            return handleImportError(documentImport, "O arquivo CSV está vazio.");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> filteredHeaders = validateHeaders(reader);
            if (filteredHeaders == null) {
                return handleImportError(documentImport, "O cabeçalho do arquivo CSV está vazio ou incorreto.");
            }

            CSVParser csvParser = new CSVParser(
                    reader, CSVFormat.DEFAULT.withDelimiter(';').withHeader(filteredHeaders.toArray(new String[0])));

            List<CSVRecord> validRecords = filterValidRecords(csvParser.getRecords());

            if (validRecords.isEmpty()) {
                return handleImportError(documentImport, "O arquivo CSV deve conter pelo menos uma linha de dados.");
            }

            logger.info("Processando registros CSV para o arquivo: {}", fileName);
            processCSVRecords(validRecords, documentImport);
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Erro ao ler o arquivo CSV: ", e);
            return handleImportError(documentImport, "Erro ao ler o arquivo CSV.");
        }

        documentImport.setMessage(documentImport.getImportErrors()
                .isEmpty() ? "Importação concluída com sucesso" : "Importação concluída com alguns erros");

        documentImportRepository.save(documentImport);

        return CompletableFuture.completedFuture(documentImport);
    }

    private List<String> validateHeaders(BufferedReader reader) throws IOException {
        logger.debug("Validando cabeçalhos do CSV");
        String headerLine = reader.readLine();
        if (headerLine == null || headerLine.trim().isEmpty()) {
            logger.warn("O cabeçalho do CSV está vazio ou ausente");
            return null;
        }

        String[] headers = headerLine.split(";");
        List<String> filteredHeaders = new ArrayList<>();
        for (String header : headers) {
            if (header != null && !header.trim().isEmpty()) {
                filteredHeaders.add(header.trim());
            }
        }

        String[] expectedHeaders = {"subject", "content", "location", "author", "translator_email"};
        if (filteredHeaders.size() != expectedHeaders.length) {
            logger.warn("O cabeçalho do CSV não corresponde ao número esperado de colunas");
            return null;
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            if (!filteredHeaders.get(i).equals(expectedHeaders[i])) {
                logger.warn("O cabeçalho do CSV não corresponde aos nomes das colunas esperadas ou está fora de ordem");
                return null;
            }
        }

        return filteredHeaders;
    }

    private List<CSVRecord> filterValidRecords(List<CSVRecord> records) {
        logger.debug("Filtrando registros CSV válidos");
        return records.stream()
                .filter(record -> record.stream().anyMatch(field -> field != null && !field.trim().isEmpty()))
                .toList();
    }

    private void processCSVRecords(List<CSVRecord> records, DocumentImport documentImport) {
        int rowNum = 1;

        for (CSVRecord record : records) {
            rowNum++;
            String subject = record.get("subject");
            String content = record.get("content");
            String location = record.get("location");
            String author = record.get("author");
            String translatorEmail = record.get("translator_email");

            logger.info("Validando os campos obrigatórios");
            if (subject.isEmpty() || content.isEmpty() || author.isEmpty() || translatorEmail.isEmpty()) {
                String error = "Linha " + rowNum + ": Campos obrigatórios estão faltando.";
                documentImport.addImportError(new ImportError(error, documentImport));
                continue;
            }

            if (location.isEmpty()) {
                location = openAIService.detectLanguage(content.substring(0, Math.min(content.length(), 40)));
                logger.debug("Idioma detectado para o conteúdo na linha {}: {}", rowNum, location);
            }

            Translator translator = translatorRepository.findByEmail(translatorEmail);
            if (translator == null) {
                String error = "Linha " + rowNum + ": Tradutor não encontrado para o email: " + translatorEmail;
                documentImport.addImportError(new ImportError(error, documentImport));
                continue;
            }

            Document document = new Document(subject, content, location, author, translator, documentImport);
            validateAndAddDocument(document, documentImport, rowNum);
        }
    }

    private void validateAndAddDocument(Document document, DocumentImport documentImport, int rowNum) {
        Set<ConstraintViolation<Document>> violations = validator.validate(document);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Document> violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            String error = "Linha " + rowNum + ": " + sb;
            documentImport.addImportError(new ImportError(error, documentImport));
            return;
        }
        documentImport.addDocument(document);
    }

    private CompletableFuture<DocumentImport> handleImportError(DocumentImport documentImport, String errorMessage) {
        documentImport.addImportError(new ImportError(errorMessage, documentImport));
        documentImport.setMessage("Erro durante a importação");
        documentImportRepository.save(documentImport);
        return CompletableFuture.completedFuture(documentImport);
    }
}