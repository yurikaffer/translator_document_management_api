package com.bureauworks.translator_document_management.service;

import com.bureauworks.translator_document_management.repository.TranslatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bureauworks.translator_document_management.entity.Translator;
import com.bureauworks.translator_document_management.exception.EmailAlreadyExistsException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

@Service
public class TranslatorService {

    @Autowired
    private TranslatorRepository translatorRepository;

    @Autowired
    private Validator validator;

    //@Cacheable(value = "translators", key = "#pageable")
    public Page<Translator> findAll(Pageable pageable) {
        return translatorRepository.findAllByOrderByCreateAtDesc(pageable);
    }

    //@Cacheable(value = "translators", key = "#text")
    public Page<Translator> searchTranslators(String text, Pageable pageable) {
        return translatorRepository.searchByText(text, pageable);
    }

    //@CachePut(value = "translators", key = "#translator.id")
    public Translator save(Translator translator) {
        validateData(translator);

        // Validação de email
        if (translatorRepository.existsByEmail(translator.getEmail())) {
            throw new EmailAlreadyExistsException("Este email já está em uso.");
        }
        return translatorRepository.save(translator);
    }

    public Translator update(Translator translator, Translator newTranslator) {
        validateData(newTranslator);

        // Validação de email
        if (!newTranslator.getEmail().equals(translator.getEmail()) &&
                translatorRepository.existsByEmail(newTranslator.getEmail()))
        {
                throw new EmailAlreadyExistsException("Este email já está em uso.");
        }

        translator.setName(newTranslator.getName());
        translator.setEmail(newTranslator.getEmail());
        translator.setSourceLanguage(newTranslator.getSourceLanguage());
        translator.setTargetLanguage(newTranslator.getTargetLanguage());
        return translatorRepository.save(translator);
    }

    private void validateData(Translator translator) {
        Set<ConstraintViolation<Translator>> violations = validator.validate(translator);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Translator> violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }

    //@Cacheable(value = "translators", key = "#id")
    public Translator findById(Long id) {
        return translatorRepository.findById(id).orElse(null);
    }

    //@CacheEvict(value = "translators", key = "#id")
    public void deleteById(Long id) {
        translatorRepository.deleteById(id);
    }
}
