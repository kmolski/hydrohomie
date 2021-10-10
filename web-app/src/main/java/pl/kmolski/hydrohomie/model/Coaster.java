package pl.kmolski.hydrohomie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;

@Table("coasters")
public class Coaster {

    @Id
    private final String deviceName;
    private String displayName;
    private String description;
    private String place;

    private Float initLoad;
    private Instant inactiveSince;

    public Coaster(@NonNull String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Coaster setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Coaster setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPlace() {
        return place;
    }

    public Coaster setPlace(String place) {
        this.place = place;
        return this;
    }

    public Float getInitLoad() {
        return initLoad;
    }

    public Coaster setInitLoad(Float initLoad) {
        this.initLoad = initLoad;
        return this;
    }

    public Instant getInactiveSince() {
        return inactiveSince;
    }

    public Coaster setInactiveSince(Instant inactiveSince) {
        this.inactiveSince = inactiveSince;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coaster coaster = (Coaster) o;
        return deviceName.equals(coaster.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceName);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Coaster.class.getSimpleName() + "[", "]")
                .add("deviceName='" + deviceName + "'")
                .add("displayName='" + displayName + "'")
                .add("description='" + description + "'")
                .add("place='" + place + "'")
                .add("inactiveSince='" + inactiveSince + "'")
                .toString();
    }
}
