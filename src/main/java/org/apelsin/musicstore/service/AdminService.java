package org.apelsin.musicstore.service;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.*;
import org.apelsin.musicstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminActionLogRepository actionLogRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;

    public List<AdminActionLog> getAllLogs() {
        return actionLogRepository.findAllByOrderByActionTimestampDesc();
    }

    public List<AdminActionLog> getLogsByAdmin(Long adminId) {
        return actionLogRepository.findByAdmin_UserId(adminId);
    }

    public List<AdminActionLog> getLogsByEntity(String entity, Long entityId) {
        return actionLogRepository.findByTargetEntityAndTargetId(entity, entityId);
    }

    @Transactional
    public void logAction(User admin, String actionType, String targetEntity, Long targetId, String details) {
        AdminActionLog log = new AdminActionLog();
        log.setAdmin(admin);
        log.setActionType(actionType);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        log.setActionDetails(details);
        actionLogRepository.save(log);
    }

    @Transactional
    public Track uploadTrack(String title, Double price, Long artistId, Long albumId, Long adminId, String filePath) {
        Artist artist = artistRepository.findById(artistId).orElseThrow();
        Album album = albumId != null ? albumRepository.findById(albumId).orElse(null) : null;
        User admin = userRepository.findById(adminId).orElseThrow();

        Track track = new Track();
        track.setTrackTitle(title);
        track.setTrackPrice(price);
        track.setTrackArtist(artist);
        track.setTrackAlbum(album);
        track.setTrackUploadedBy(admin);
        track.setTrackFilePath(filePath);
        track.setTrackDownloadCount(0L);
        
        Track saved = trackRepository.save(track);
        
        logAction(admin, "UPLOAD", "Track", saved.getTrackId(), "Загружен трек: " + title);
        
        return saved;
    }

    @Transactional
    public void deleteTrack(Long trackId, Long adminId) {
        Track track = trackRepository.findById(trackId).orElseThrow();
        User admin = userRepository.findById(adminId).orElseThrow();
        
        logAction(admin, "DELETE", "Track", trackId, "Удален трек: " + track.getTrackTitle());
        
        trackRepository.deleteById(trackId);
    }

    @Transactional
    public Track updateTrack(Long trackId, String title, Double price) {
        Track track = trackRepository.findById(trackId).orElseThrow();
        
        if (title != null) track.setTrackTitle(title);
        if (price != null) track.setTrackPrice(price);
        
        return trackRepository.save(track);
    }

    @Transactional
    public License addLicense(Long trackId, String contractNumber, String ownerName, 
            String startDate, String expirationDate, String terms) {
        Track track = trackRepository.findById(trackId).orElseThrow();
        
        License license = new License();
        license.setLicenseTrack(track);
        license.setLicenseContractNumber(contractNumber);
        license.setLicenseOwnerName(ownerName);
        license.setLicenseStartDate(java.time.LocalDate.parse(startDate));
        license.setLicenseExpirationDate(java.time.LocalDate.parse(expirationDate));
        license.setLicenseTerms(terms);
        
        return licenseRepository.save(license);
    }

    public License getTrackLicense(Long trackId) {
        return licenseRepository.findByLicenseTrack_TrackId(trackId).orElse(null);
    }

    public List<Track> getMostDownloadedTracks(int limit) {
        return trackRepository.findAllByOrderByTrackDownloadCountDesc().stream().limit(limit).toList();
    }

    public List<AdminActionLog> getAdminLiability(Long trackId) {
        return actionLogRepository.findByTargetEntityAndTargetId("Track", trackId);
    }
}
