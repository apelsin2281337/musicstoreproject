package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByLicenseTrack_TrackId(Long trackId);
}