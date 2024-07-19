package com.bureauworks.translator_document_management.repository;

import com.bureauworks.translator_document_management.entity.ImportError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {
}