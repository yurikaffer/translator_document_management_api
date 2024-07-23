package com.bureauworks.translator_document_management.controller;

import com.bureauworks.translator_document_management.entity.DocumentImport;
import com.bureauworks.translator_document_management.service.DocumentImportService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/documentImport")
@Tag(name = "DocumentImportController", description = "Gerenciamento de importações de documentos")
public class DocumentImportController {

    @Autowired
    private DocumentImportService documentImportService;

    @Operation(summary = "Obtém todas as importações",
            description = "Retorna uma lista paginada de todas as importações de documentos")
    @GetMapping
    public ResponseEntity<Page<DocumentImport>> getAllDocumentImports(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentImport> documentImports = documentImportService.findAll(pageable);
        return new ResponseEntity<>(documentImports, HttpStatus.OK);
    }

    @Operation(summary = "Pesquisa importações de documentos",
            description = "Retorna uma lista paginada de importações que correspondem ao texto de pesquisa")
    @GetMapping("/search")
    public ResponseEntity<Page<DocumentImport>> searchDocumentImports(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentImport> documentImports = documentImportService.searchImports(text, pageable);
        return new ResponseEntity<>(documentImports, HttpStatus.OK);
    }

    @Operation(summary = "Deleta uma importação de documento",
            description = "Remove uma importação de documento existente pelo seu ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentImport(@PathVariable Long id) {
        DocumentImport documentImport = documentImportService.findById(id);
        if (documentImport == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        documentImportService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Importa documentos",
            description = "Importa documentos a partir de um arquivo CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documentos importados com sucesso",
                    content = @Content(schema = @Schema(implementation = DocumentImport.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/upload")
    public CompletableFuture<DocumentImport> uploadDocuments(
            @Parameter(description = "Arquivo CSV para importação de documentos", required = true,
                    content = @Content(mediaType = "multipart/form-data"),
                    schema = @Schema(type = "string", format = "binary"))
            @RequestParam("file") MultipartFile file) {
        return documentImportService.saveDocumentsFromCSV(file);
    }
}
