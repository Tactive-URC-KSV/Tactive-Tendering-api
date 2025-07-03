package com.ksv.tactive.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.dao.RegionDao;
import com.ksv.tactive.entity.Region;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {
	
	@Autowired
	private RegionDao regionRepo;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	
	@PostConstruct
	public void loadCountries() {
		try {
			if(regionRepo.count()==0) {
				InputStream inputStream=new ClassPathResource("data/countries.json").getInputStream();
				List<Region> regions=objectMapper.readValue(inputStream, new TypeReference<List<Region>>(){});
				for(Region region :regions) {
					region.setId(UUID.randomUUID().toString());
				}
				regionRepo.saveAll(regions);
				System.out.println("regions saved successfully");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
