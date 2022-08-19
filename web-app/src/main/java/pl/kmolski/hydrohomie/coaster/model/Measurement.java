package pl.kmolski.hydrohomie.coaster.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Models a single volume measurement made by the coaster.
 *
 * @param id the sequential measurement ID
 * @param deviceName the coaster device ID
 * @param volume the measured volume
 * @param timestamp the creation time of the measurement
 */
@Table("measurements")
public record Measurement(@Id Integer id, String deviceName, float volume, Instant timestamp) {}
