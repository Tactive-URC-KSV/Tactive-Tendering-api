package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.project.dao.LanguageDao;
import com.ksv.tactive.project.entity.Language;

@Component
public class LanguageDataLoader implements MasterDataLoader<Language> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LanguageDao languageDao;

    @Override
    public void loadData() throws IOException, DataAccessException {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/Language.json")) {
            if (inputStream == null) {
                throw new IOException("Resource file 'language.json' not found");
            }
            List<Language> languagesData = objectMapper.readValue(inputStream, new TypeReference<List<Language>>() {});
            for (Language language : languagesData) {
                saveLanguage(language);
            }
        }
    }
    private void saveLanguage(Language languageInput) {
        Language existingLanguage = languageDao.findByLanguageCode(languageInput.getLanguageCode());
        if (existingLanguage == null) {
            languageInput.setId(UUID.randomUUID().toString());
            languageInput.setActive(true);
            languageDao.save(languageInput);
        }
    }
}