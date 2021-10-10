package pl.kmolski.hydrohomie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("measurements")
public record Measurement(@Id Integer id, String deviceName, float volume, Instant timestamp) {}
