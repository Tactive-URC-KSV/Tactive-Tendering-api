package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.Language;
@Repository
public interface LanguageDao extends JpaRepository<Language, String>{
    boolean existsByLanguageIgnoreCase(String language);
}
