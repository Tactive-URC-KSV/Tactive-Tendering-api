package com.ksv.tactive.fileservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksv.tactive.dto.ExcelExtractBOQ;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class PdfAiExtractionService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public List<ExcelExtractBOQ> getExtractedData(MultipartFile boqFile, Map<String, List<String>> columnMapping,
                                                  int startRow, int endRow, int startPage) throws IOException {
        String base64Pdf = Base64.getEncoder().encodeToString(boqFile.getBytes());
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = "Extract the BOQ (Bill of Quantities) table from this PDF document. " +
                "Please return the result ONLY as a JSON array of objects. " +
                "Use the following keys for the JSON objects based on the mapping: boqCode, boqName, uom, quantity. " +
                "Also include a 'sno' integer field starting from 1. " +
                "Do not include markdown formatting like ```json.";

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts1 = new HashMap<>();
        parts1.put("text", prompt);

        Map<String, Object> parts2 = new HashMap<>();
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mimeType", "application/pdf");
        inlineData.put("data", base64Pdf);
        parts2.put("inlineData", inlineData);

        contents.put("parts", Arrays.asList(parts1, parts2));
        requestBody.put("contents", Arrays.asList(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseBody = response.getBody();
            return parseAiResponse(responseBody);
        } catch (Exception e) {
            throw new IOException("Failed to extract data using AI", e);
        }
    }

    private List<ExcelExtractBOQ> parseAiResponse(String responseBody) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0) {
                String text = parts.get(0).path("text").asText();
                text = text.replaceAll("```json", "").replaceAll("```", "").trim();
                
                JsonNode arrayNode = mapper.readTree(text);
                List<ExcelExtractBOQ> boqList = new ArrayList<>();
                if (arrayNode.isArray()) {
                    for (JsonNode node : arrayNode) {
                        ExcelExtractBOQ boq = new ExcelExtractBOQ();
                        if (node.has("sno")) boq.setSno(node.get("sno").asInt());
                        if (node.has("boqCode")) boq.setBoqCode(node.get("boqCode").asText());
                        if (node.has("boqName")) boq.setBoqName(node.get("boqName").asText());
                        if (node.has("uom")) boq.setUom(node.get("uom").asText());
                        if (node.has("quantity")) boq.setQuantity(node.get("quantity").asDouble());
                        boq.setLastLevel(boq.getQuantity() > 0 || (boq.getUom() != null && !boq.getUom().isBlank()));
                        boqList.add(boq);
                    }
                }
                return boqList;
            }
        }
        return new ArrayList<>();
    }
}
