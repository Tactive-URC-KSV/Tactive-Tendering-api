package com.ksv.tactive.dto;
import java.util.Map;
import com.ksv.tactive.project.entity.Template;
import lombok.Data;
@Data
public class TemplateDto {
	private Template template;
	private Map<String, String> templateMapping;
}
