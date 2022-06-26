package pl.kmolski.hydrohomie.coaster.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.StringJoiner;

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

    public ZoneId getTimezone() {
        return timezone;
    }

    public Coaster setTimezone(ZoneId timezone) {
        this.timezone = timezone;
        return this;
    }

    public String getPlace() {
        return place;
    }

    public Coaster setPlace(String place) {
        this.place = place;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public Coaster setOwner(String owner) {
        this.owner = owner;
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
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
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
                .add("timezone=" + timezone)
                .add("place='" + place + "'")
                .add("owner='" + owner + "'")
                .add("initLoad=" + initLoad)
                .add("inactiveSince=" + inactiveSince)
                .toString();
    }
}
