package com.ksv.tactive.dto;
import java.util.List;
import com.ksv.tactive.project.entity.Projects;
import lombok.Data;
@Data
public class ProjectInputDTO {
	private Projects project;
	private String sectorId;
	private String regionId;
	private String uomId;
	private List<String> scopeOfPackageIds;
}
