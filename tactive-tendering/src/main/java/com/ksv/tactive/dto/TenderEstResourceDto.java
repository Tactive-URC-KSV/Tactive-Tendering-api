package com.ksv.tactive.dto;
import com.ksv.tactive.project.entity.TenderEstimation;
import com.ksv.tactive.project.entity.TenderEstimationCostDetails;
import lombok.Data;
import java.util.List;
@Data
public class TenderEstResourceDto {
    private TenderEstimation tenderEstimation;
    private TenderEstimationCostDetails costDetails; 
    private List<TenderEstimationCostDetails> costDetailsList;
    public TenderEstimationCostDetails getCostDetailsForBoq(String boqId) {
        if (costDetailsList != null) {
            return costDetailsList.stream()
                .filter(cd -> boqId.equals(cd.getBoq().getId()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}