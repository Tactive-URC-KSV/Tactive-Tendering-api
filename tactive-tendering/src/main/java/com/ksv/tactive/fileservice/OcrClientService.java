package com.ksv.tactive.fileservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
public class OcrClientService {

    @Value("${ocr.service.url:http://localhost:8000}")
    private String ocrServiceUrl;

    private final RestTemplate restTemplate;

    public OcrClientService() {
        this.restTemplate = new RestTemplate();
        // Ideally set up timeouts for RestTemplate here if needed
    }

    public List<List<List<String>>> extractTableFromImage(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "page.png";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = ocrServiceUrl + "/extract_table";
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("tables")) {
                    return (List<List<List<String>>>) responseBody.get("tables");
                }
            }
        } catch (Exception e) {
            System.err.println("OCR Service call failed: " + e.getMessage());
            throw e;
        }

        return Collections.emptyList();
    }
}
