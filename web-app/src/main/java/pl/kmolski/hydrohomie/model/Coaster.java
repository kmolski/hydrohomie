package pl.kmolski.hydrohomie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.StringJoiner;

@Table("coasters")
public class Coaster {

    @Id
    private final String deviceName;
    private String displayName;
    private String description;
    private String place;

    public Coaster(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
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
                .toString();
    }
}
