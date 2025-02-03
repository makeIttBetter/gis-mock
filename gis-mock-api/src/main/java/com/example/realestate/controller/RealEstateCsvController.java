package com.example.realestate.controller;

import com.example.realestate.dto.RealEstateCsvUploadResult;
import com.example.realestate.service.RealEstateCsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        log.info("Received CSV file upload");
        RealEstateCsvUploadResult result = realEstateCsvService.processCsv(file);
        return ResponseEntity.ok(result);
    }
}
