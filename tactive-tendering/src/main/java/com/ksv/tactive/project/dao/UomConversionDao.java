package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.Uom;
import com.ksv.tactive.project.entity.UomConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UomConversionDao extends JpaRepository<UomConversion, UUID> {
    List<UomConversion> findAllByBaseUom_Id(UUID baseUomId);
    boolean existsByBaseUomAndConversionUom(Uom baseUom, Uom conversionUom);
}
