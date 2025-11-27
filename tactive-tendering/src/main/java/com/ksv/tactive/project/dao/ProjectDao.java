package com.ksv.tactive.project.dao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.dto.ProjectInfoDto;
import com.ksv.tactive.project.entity.Projects;
@Repository
public interface ProjectDao extends JpaRepository<Projects, String> {
	Projects findByProjectCode(String projectCode);
	@Query("SELECT new com.ksv.tactive.dto.ProjectInfoDto(" +
		       "p.id, p.projectCode, p.projectName, p.startDate, p.endDate, " +
		       "s.sectorName, r.regionName, p.estimatedValue, ps.status, p.createdAt, p.archieve) " +
		       "FROM Projects p " +
		       "LEFT JOIN p.sector s " +
		       "LEFT JOIN p.region r " +
		       "LEFT JOIN p.projectStatus ps")
		List<ProjectInfoDto> findAllProjectInfo();
	@Query("SELECT new com.ksv.tactive.dto.ProjectInfoDto(" +
		       "p.id, p.projectCode, p.projectName, p.startDate, p.endDate, " +
		       "s.sectorName, r.regionName, p.estimatedValue, ps.status, p.createdAt, p.archieve) " +
		       "FROM Projects p " +
		       "LEFT JOIN p.sector s " +
		       "LEFT JOIN p.region r " +
		       "LEFT JOIN p.projectStatus ps where p.id = :id")
	ProjectInfoDto findAllProjectInfoById(@Param("id") String id);
}
