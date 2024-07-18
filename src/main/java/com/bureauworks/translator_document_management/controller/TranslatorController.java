package com.bureauworks.translator_document_management.controller;

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

@RestController
@RequestMapping("/translators")
public class TranslatorController {

    @Autowired
    private TranslatorService translatorService;

    @GetMapping
    public ResponseEntity<Page<Translator>> getAllTranslators(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Translator> translators = translatorService.findAll(pageable);
        return new ResponseEntity<>(translators, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Translator>> searchTranslators(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Translator> translators = translatorService.searchTranslators(text, pageable);
        return new ResponseEntity<>(translators, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createTranslator(@RequestBody Translator translator) {
        try {
            Translator createdTranslator = translatorService.save(translator);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTranslator);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Translator> getTranslatorById(@PathVariable Long id) {
        Translator translator = translatorService.findById(id);
        if (translator == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(translator);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTranslator(@PathVariable Long id, @RequestBody Translator translatorDetails) {
        try {
            Translator translator = translatorService.findById(id);
            if (translator == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            translator.setName(translatorDetails.getName());
            translator.setEmail(translatorDetails.getEmail());
            translator.setSourceLanguage(translatorDetails.getSourceLanguage());
            translator.setTargetLanguage(translatorDetails.getTargetLanguage());
            Translator updatedTranslator = translatorService.save(translator);
            return ResponseEntity.ok(updatedTranslator);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranslator(@PathVariable Long id) {
        Translator translator = translatorService.findById(id);
        if (translator == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        translatorService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
