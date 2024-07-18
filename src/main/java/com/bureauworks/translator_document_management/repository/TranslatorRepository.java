package com.bureauworks.translator_document_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.bureauworks.translator_document_management.entity.Translator;

@Repository
public interface TranslatorRepository extends JpaRepository<Translator, Long> {
    boolean existsByEmail(String email);
    Translator findByEmail(String email);

    @Query("SELECT t FROM Translator t WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :text, '%'))")
    Page<Translator> searchByText(@Param("text") String text, Pageable pageable);
}
