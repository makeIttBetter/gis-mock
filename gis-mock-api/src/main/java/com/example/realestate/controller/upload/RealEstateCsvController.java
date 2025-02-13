package com.example.realestate.controller.upload;

import com.example.realestate.dto.upload.RealEstateCsvUploadResult;
import com.example.realestate.service.RealEstateCsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/real-estate")
public class RealEstateCsvController {

    private final RealEstateCsvService realEstateCsvService;

    public RealEstateCsvController(RealEstateCsvService realEstateCsvService) {
        this.realEstateCsvService = realEstateCsvService;
    }

    @PostMapping("/upload")
    public ResponseEntity<RealEstateCsvUploadResult> uploadCsv(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/real-estate/upload");
        RealEstateCsvUploadResult result = realEstateCsvService.processCsv(file);
        return ResponseEntity.ok(result);
    }
}
