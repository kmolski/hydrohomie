package pl.kmolski.hydrohomie.coaster.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.ZoneId;

@Data
@Table("coasters")
public class Coaster {

    @Id
    private final String deviceName;
    private String displayName;
    private String description;
    private ZoneId timezone;
    private String place;
    private String owner;

    private Float initLoad;
    private Instant inactiveSince;
}
