package org.apelsin.musicstore.controller;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.License;
import org.apelsin.musicstore.repository.LicenseRepository;
import org.apelsin.musicstore.repository.TrackRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
public class LicenseController {
    private final LicenseRepository licenseRepository;
    private final TrackRepository trackRepository;

    @GetMapping("/track/{trackId}")
    public ResponseEntity<?> getTrackLicense(@PathVariable Long trackId) {
        Optional<License> license = licenseRepository.findByLicenseTrack_TrackId(trackId);
        if (license.isPresent()) {
            return ResponseEntity.ok(license.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> addLicense(@RequestBody LicenseRequest request) {
        License license = new License();
        license.setLicenseTrack(trackRepository.findById(request.trackId).orElseThrow());
        license.setLicenseContractNumber(request.contractNumber);
        license.setLicenseOwnerName(request.ownerName);
        license.setLicenseStartDate(LocalDate.parse(request.startDate));
        license.setLicenseExpirationDate(LocalDate.parse(request.expirationDate));
        license.setLicenseTerms(request.terms);
        
        licenseRepository.save(license);
        return ResponseEntity.ok("Лицензия добавлена");
    }

    public static class LicenseRequest {
        public Long trackId;
        public String contractNumber;
        public String ownerName;
        public String startDate;
        public String uploader;
        public String expirationDate;
        public String terms;
    }
}
