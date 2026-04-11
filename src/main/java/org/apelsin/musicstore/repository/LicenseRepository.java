package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    @Query(value = "SELECT * FROM licenses WHERE track_id = :trackId", nativeQuery = true)
    Optional<License> findByLicenseTrack_TrackId(@Param("trackId") Long trackId);

    @Query(value = "SELECT * FROM licenses WHERE license_id = :licenseId", nativeQuery = true)
    Optional<License> findByLicenseId(@Param("licenseId") Long licenseId);
}
