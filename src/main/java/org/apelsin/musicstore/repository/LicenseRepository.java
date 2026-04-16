package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.License;
import org.apelsin.musicstore.model.Track;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class LicenseRepository {

    private final DataSource dataSource;
    private final TrackRepository trackRepository;

    public LicenseRepository(DataSource dataSource, TrackRepository trackRepository) {
        this.dataSource = dataSource;
        this.trackRepository = trackRepository;
    }

    private License mapResultSetToLicense(ResultSet rs) throws SQLException {
        License license = new License();
        license.setLicenseId(rs.getLong("license_id"));
        license.setLicenseContractNumber(rs.getString("license_contract_number"));
        license.setLicenseOwnerName(rs.getString("license_owner_name"));
        license.setUploader(rs.getString("uploader"));
        license.setLicenseStartDate(rs.getDate("license_start_date").toLocalDate());
        license.setLicenseExpirationDate(rs.getDate("license_expiration_date").toLocalDate());
        license.setLicenseTerms(rs.getString("license_terms"));

        Long trackId = rs.getLong("track_id");
        if (!rs.wasNull()) {
            trackRepository.findById(trackId).ifPresent(license::setLicenseTrack);
        }

        return license;
    }

    public License save(License license) {
        String sql = "INSERT INTO licenses (track_id, license_contract_number, license_owner_name, uploader, license_start_date, license_expiration_date, license_terms) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, license.getLicenseTrack().getTrackId());
            ps.setString(2, license.getLicenseContractNumber());
            ps.setString(3, license.getLicenseOwnerName());
            ps.setString(4, license.getUploader());
            ps.setDate(5, Date.valueOf(license.getLicenseStartDate()));
            ps.setDate(6, Date.valueOf(license.getLicenseExpirationDate()));
            ps.setString(7, license.getLicenseTerms());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    license.setLicenseId(rs.getLong(1));
                }
            }
            return license;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save license", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM licenses WHERE license_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete license", e);
        }
    }

    public Optional<License> findById(Long id) {
        String sql = "SELECT * FROM licenses WHERE license_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLicense(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find license by id", e);
        }
    }

    public List<License> findAll() {
        String sql = "SELECT * FROM licenses";
        List<License> licenses = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                licenses.add(mapResultSetToLicense(rs));
            }
            return licenses;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all licenses", e);
        }
    }

    public Optional<License> findByLicenseTrack_TrackId(Long trackId) {
        String sql = "SELECT * FROM licenses WHERE track_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, trackId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLicense(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find license by track id", e);
        }
    }

    public Optional<License> findByLicenseId(Long licenseId) {
        return findById(licenseId);
    }

    public License update(License license) {
        String sql = "UPDATE licenses SET track_id = ?, license_contract_number = ?, license_owner_name = ?, uploader = ?, license_start_date = ?, license_expiration_date = ?, license_terms = ? WHERE license_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, license.getLicenseTrack().getTrackId());
            ps.setString(2, license.getLicenseContractNumber());
            ps.setString(3, license.getLicenseOwnerName());
            ps.setString(4, license.getUploader());
            ps.setDate(5, Date.valueOf(license.getLicenseStartDate()));
            ps.setDate(6, Date.valueOf(license.getLicenseExpirationDate()));
            ps.setString(7, license.getLicenseTerms());
            ps.setLong(8, license.getLicenseId());

            ps.executeUpdate();
            return license;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update license", e);
        }
    }
}
