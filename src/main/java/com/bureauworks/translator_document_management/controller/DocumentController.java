package com.bureauworks.translator_document_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bureauworks.translator_document_management.entity.Document;
import com.bureauworks.translator_document_management.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "DocumentController", description = "Gerenciamento de documentos")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Operation(summary = "Obtém todos os documentos", description = "Retorna uma lista paginada de todos os documentos")
    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocuments(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = documentService.findAll(pageable);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @Operation(summary = "Pesquisa documentos",
            description = "Retorna uma lista paginada de documentos que correspondem ao texto de pesquisa")
    @GetMapping("/search")
    public ResponseEntity<Page<Document>> searchDocuments(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = documentService.searchDocuments(text, pageable);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @Operation(summary = "Cria um novo documento",
            description = "Cria um novo documento e retorna os detalhes do documento criado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public ResponseEntity<?> createDocument(@RequestBody Document document) {
        try {
            Document createdDocument = documentService.save(document);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Obtém um documento pelo ID",
            description = "Retorna os detalhes de um documento pelo seu ID")
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document document = documentService.findById(id);
        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(document);
    }

    @Operation(summary = "Atualiza um documento",
            description = "Atualiza os detalhes de um documento existente pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado",
                    content = @Content(schema = @Schema(implementation = Void.class)))
    })
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

    @Operation(summary = "Deleta um documento", description = "Remove um documento existente pelo seu ID")
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