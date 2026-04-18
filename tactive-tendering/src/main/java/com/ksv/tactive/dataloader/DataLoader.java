package com.ksv.tactive.dataloader;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    @Autowired
    private List<MasterDataLoader<?>> loaders;
    @Override
    public void run(String... args) {
        logger.info("Starting data loading process");
        long startTime = System.currentTimeMillis();
        loaders.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .forEach(loader -> {
                    long loaderStart = System.currentTimeMillis();
                    try {
                        loader.loadData();

                        long loaderEnd = System.currentTimeMillis();
                        double seconds = (loaderEnd - loaderStart) / 1000.0;

                        logger.info("Executed loader: {} in {} seconds",
                                loader.getClass().getSimpleName(), seconds);

                    } catch (Exception e) {
                        logger.error("Failed loader: {}", loader.getClass().getSimpleName(), e);
                    }
                });
        long endTime = System.currentTimeMillis();
        double totalSeconds = (endTime - startTime) / 1000.0;
        logger.info("Data loading completed in {} seconds", totalSeconds);
    }
}