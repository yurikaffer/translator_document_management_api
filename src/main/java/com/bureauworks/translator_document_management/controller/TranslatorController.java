package com.bureauworks.translator_document_management.controller;

import com.bureauworks.translator_document_management.entity.Document;
import com.bureauworks.translator_document_management.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bureauworks.translator_document_management.entity.Translator;
import com.bureauworks.translator_document_management.exception.EmailAlreadyExistsException;
import com.bureauworks.translator_document_management.service.TranslatorService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/translators")
@Tag(name = "TranslatorController", description = "Gerenciamento de tradutores")
public class TranslatorController {

    @Autowired
    private TranslatorService translatorService;

    @Autowired
    private DocumentService documentService;

    @Operation(summary = "Obtém todos os tradutores", description = "Retorna uma lista paginada de todos os tradutores")
    @GetMapping
    public ResponseEntity<Page<Translator>> getAllTranslators(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Translator> translators = translatorService.findAll(pageable);
        return new ResponseEntity<>(translators, HttpStatus.OK);
    }

    @Operation(summary = "Pesquisa tradutores",
            description = "Retorna uma lista paginada de tradutores que correspondem ao texto de pesquisa")
    @GetMapping("/search")
    public ResponseEntity<Page<Translator>> searchTranslators(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Translator> translators = translatorService.searchTranslators(text, pageable);
        return new ResponseEntity<>(translators, HttpStatus.OK);
    }

    @Operation(summary = "Cria um novo tradutor",
            description = "Cria um novo tradutor e retorna os detalhes do tradutor criado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tradutor criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Translator.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já existe",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public ResponseEntity<?> createTranslator(@RequestBody Translator translator) {
        try {
            Translator createdTranslator = translatorService.save(translator);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTranslator);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Operation(summary = "Obtém um tradutor pelo ID",
            description = "Retorna os detalhes de um tradutor através do seu ID")
    @GetMapping("/{id}")
    public ResponseEntity<Translator> getTranslatorById(@PathVariable Long id) {
        Translator translator = translatorService.findById(id);
        if (translator == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(translator);
    }

    @Operation(summary = "Atualiza um tradutor",
            description = "Atualiza os detalhes de um tradutor existente através do seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tradutor atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = Translator.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Tradutor não encontrado",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já existe",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTranslator(@PathVariable Long id, @RequestBody Translator newTranslator) {
        try {
            Translator translator = translatorService.findById(id);
            if (translator == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Translator updatedTranslator = translatorService.update(translator, newTranslator);
            return ResponseEntity.ok(updatedTranslator);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Operation(summary = "Deleta um tradutor",
            description = "Remove um tradutor existente através do seu ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranslator(@PathVariable Long id) {
        Translator translator = translatorService.findById(id);
        if (translator == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Document> documents = documentService.findAllByTranslatorId(id);
        if (!documents.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        translatorService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
