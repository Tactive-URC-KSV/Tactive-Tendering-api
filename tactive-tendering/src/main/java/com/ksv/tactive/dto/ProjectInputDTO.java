package com.ksv.tactive.dto;
import java.util.List;
import com.ksv.tactive.project.entity.Projects;
import lombok.Data;
@Data
public class ProjectInputDTO {
	private String projectName;
	private String shortName;
	private String startDate;
	private String endDate;
	private boolean needFeasibility;
	private String sectorId;
	private String regionId;
	private String uomId;
	private String companyId;
	private List<String> scopeOfPackageIds;
	private List<ProjectAddressDto> addresses;
	private List<ProjectTechFieldDto> techFields;
}
