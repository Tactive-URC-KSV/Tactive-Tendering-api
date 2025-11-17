package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.Template;
@Repository
public interface TemplateDao extends JpaRepository<Template, String>{
	Template findByTemplateName(String templateName);
}
