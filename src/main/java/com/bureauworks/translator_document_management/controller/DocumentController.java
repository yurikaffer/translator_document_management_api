package com.bureauworks.translator_document_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bureauworks.translator_document_management.entity.Document;
import com.bureauworks.translator_document_management.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocuments(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = documentService.findAll(pageable);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Document>> searchDocuments(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = documentService.searchDocuments(text, pageable);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createDocument(@RequestBody Document document) {
        try {
            Document createdDocument = documentService.save(document);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public CompletableFuture<String> uploadDocuments(@RequestParam("file") MultipartFile file) {
        return documentService.saveDocumentsFromCSV(file)
                .thenApply(messages -> {
                    if (messages.isEmpty()) {
                        return "Todos os documentos foram cadastrados com sucesso.";
                    } else {
                        return "Documentos cadastrados! exceto os documentos:\n" + String.join("\n", messages);
                    }
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document document = documentService.findById(id);
        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(document);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestBody Document documentDetails) {
        try {
            Document document = documentService.findById(id);

            if (document == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            document.setSubject(documentDetails.getSubject());
            document.setContent(documentDetails.getContent());
            document.setLocation(documentDetails.getLocation());
            document.setAuthor(documentDetails.getAuthor());

            Document updatedDocument = documentService.save(document);

            return ResponseEntity.ok(updatedDocument);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Document document = documentService.findById(id);
        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        documentService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}