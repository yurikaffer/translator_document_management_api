package com.bureauworks.translator_document_management.repository;

import com.bureauworks.translator_document_management.entity.DocumentImport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentImportRepository extends JpaRepository<DocumentImport, Long> {

    @Query("SELECT d FROM DocumentImport d WHERE " +
            "LOWER(d.fileName) LIKE LOWER(CONCAT('%', :text, '%'))")
    Page<DocumentImport> searchByText(@Param("text") String text, Pageable pageable);
}
