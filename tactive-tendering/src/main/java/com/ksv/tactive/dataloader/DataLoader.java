package com.ksv.tactive.dataloader;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private List<MasterDataLoader<?>> loaders;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data loading process");
        for (MasterDataLoader<?> loader : loaders) {
            try {
                loader.loadData();
                logger.info("Successfully executed loader: {}", loader.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Failed to execute loader: {}", loader.getClass().getSimpleName(), e);
            }
        }
        logger.info("Data loading process completed");
    }
}