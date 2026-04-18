package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.InternalBoq;
import com.ksv.tactive.project.entity.InternalBoqItem;
import com.ksv.tactive.project.entity.TenderEstimation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternalBoqItemDao extends JpaRepository<InternalBoqItem, UUID> {
    List<InternalBoqItem> findByTenderEstimation(TenderEstimation tenderEst);
    Optional<InternalBoqItem> findByInternalBoqAndTenderEstimation(
            InternalBoq internalBoq,
            TenderEstimation tenderEstimation
    );
    List<InternalBoqItem> findByInternalBoq(InternalBoq boq);
}
