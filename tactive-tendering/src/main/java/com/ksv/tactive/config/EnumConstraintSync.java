package com.ksv.tactive.config;

import com.ksv.tactive.util.*;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EnumConstraintSync {

    private final JdbcTemplate jdbcTemplate;

    public EnumConstraintSync(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void syncAllEnumConstraints() {

        List<EnumConstraintConfig> configs = List.of(
                new EnumConstraintConfig(
                        FieldSection.class,
                        "sector_fields",
                        "field_section",
                        "sector_fields_field_section_check"
                ),
                new EnumConstraintConfig(
                        FieldType.class,
                        "sector_fields",
                        "field_type",
                        "sector_fields_field_type_check"
                ),
                new EnumConstraintConfig(
                        CompanyLevel.class,
                        "company",
                        "company_level",
                        "company_company_level_check"
                ),
                new EnumConstraintConfig(
                        CompanyType.class,
                        "company",
                        "type",
                        "company_type_check"
                ),
                new EnumConstraintConfig(
                        CompanyNature.class,
                        "company",
                        "nature",
                        "company_nature_check"
                ),
                new EnumConstraintConfig(
                        CompanyStatus.class,
                        "company",
                        "com_status",
                        "company_com_status_check"
                ),
                new EnumConstraintConfig(
                        QuantityType.class,
                        "tender_estimation",
                        "quantity_type",
                        "tender_estimation_quantity_type_check"
                )
        );

        for (EnumConstraintConfig config : configs) {
            syncConstraintSafely(config);
        }
    }

    private void syncConstraintSafely(EnumConstraintConfig config) {

        String enumValues = Arrays.stream(config.getEnumClass().getEnumConstants())
                .map(e -> "'" + ((Enum<?>) e).name() + "'")
                .collect(Collectors.joining(","));

        String enumValuesCsv = Arrays.stream(config.getEnumClass().getEnumConstants())
                .map(e -> ((Enum<?>) e).name())
                .collect(Collectors.joining(","));

        // 🔹 Step 1: Check invalid existing data
        String validationSql = "SELECT DISTINCT " + config.getColumnName() +
                " FROM " + config.getTableName() +
                " WHERE " + config.getColumnName() +
                " IS NOT NULL AND " + config.getColumnName() +
                " NOT IN (" + enumValues + ")";

        List<String> invalidValues = jdbcTemplate.queryForList(validationSql, String.class);

        if (!invalidValues.isEmpty()) {
            throw new RuntimeException(
                    "Invalid values found in DB for " + config.getColumnName() +
                            ": " + invalidValues
            );
        }

        String checkSql = """
                SELECT pg_get_constraintdef(oid)
                FROM pg_constraint
                WHERE conname = ?
                """;

        List<String> existing = jdbcTemplate.queryForList(
                checkSql,
                new Object[]{config.getConstraintName()},
                String.class
        );

        String expectedConstraint = "CHECK (" + config.getColumnName() +
                " IN (" + enumValues + "))";

        if (!existing.isEmpty() && existing.getFirst().contains(enumValuesCsv)) {
            return;
        }

        // 🔹 Step 3: Drop & recreate
        String dropSql = "ALTER TABLE " + config.getTableName() +
                " DROP CONSTRAINT IF EXISTS " + config.getConstraintName();

        String addSql = "ALTER TABLE " + config.getTableName() +
                " ADD CONSTRAINT " + config.getConstraintName() +
                " CHECK (" + config.getColumnName() + " IN (" + enumValues + "))";

        jdbcTemplate.execute(dropSql);
        jdbcTemplate.execute(addSql);
    }
}
