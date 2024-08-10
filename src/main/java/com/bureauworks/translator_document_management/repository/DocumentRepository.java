package com.bureauworks.translator_document_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bureauworks.translator_document_management.entity.Document;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE " +
            "LOWER(d.subject) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(d.location) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(d.author) LIKE LOWER(CONCAT('%', :text, '%')) ORDER BY d.createdAt DESC")
    Page<Document> searchByText(@Param("text") String text, Pageable pageable);

    @Query("SELECT d FROM Document d ORDER BY d.createdAt DESC")
    Page<Document> findAllByOrderByCreateAtDesc(Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.translator.id = :id")
    List<Document> findAllByTranslatorId(@Param("id") Long id);
}