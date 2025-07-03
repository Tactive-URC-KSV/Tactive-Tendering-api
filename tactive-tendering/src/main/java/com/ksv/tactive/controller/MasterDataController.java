package com.ksv.tactive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ksv.tactive.entity.ScopeOfPackage;
import com.ksv.tactive.entity.Sector;
import com.ksv.tactive.entity.UOM;
import com.ksv.tactive.service.MasterDataService;

@RestController
@RequestMapping("/tactive")
@CrossOrigin(origins = "*")
public class MasterDataController {
	
	@Autowired
	private MasterDataService masterService;
	
	@PostMapping("/sector/addSector")
	public ResponseEntity<String> addSector(@RequestBody Sector sector){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addSector(sector));
	}
	
	@PostMapping("/scope/addScope")
	public ResponseEntity<String> addScope(@RequestBody ScopeOfPackage scope){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addScope(scope));
	}
	
	@PostMapping("/uom/addUom")
	public ResponseEntity<String> addUOM(@RequestBody UOM uom){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addUom(uom));
	}
	
	@GetMapping("/uoms")
    public ResponseEntity<List<UOM>> getAllUOMs() {
        List<UOM> uoms = masterService.getAllUOMs();
        return ResponseEntity.ok(uoms);
    }

    @GetMapping("/sectors")
    public ResponseEntity<List<Sector>> getAllSectors() {
        List<Sector> sectors = masterService.getAllSector();
        return ResponseEntity.ok(sectors);
    }

    @GetMapping("/scopes")
    public ResponseEntity<List<ScopeOfPackage>> getAllScopes() {
        List<ScopeOfPackage> scopes = masterService.getAllScopes();
        return ResponseEntity.ok(scopes);
    }

}
