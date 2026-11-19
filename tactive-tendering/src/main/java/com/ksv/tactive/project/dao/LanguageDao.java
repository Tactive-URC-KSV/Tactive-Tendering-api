package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.Language;

import java.util.UUID;

@Repository
public interface LanguageDao extends JpaRepository<Language, UUID>{
    boolean existsByLanguageIgnoreCase(String language);
    Language findByLanguageCode(String languageCode);
}
