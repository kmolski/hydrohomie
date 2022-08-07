package pl.kmolski.hydrohomie.coaster.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZoneId;

@Data
public class UpdateCoasterDetailsDto {

    @Size(max = 127, message = "The display name must be at most 127 characters long")
    private String displayName;

    @Size(max = 511, message = "The description must be at most 511 characters long")
    private String description;

    @NotNull
    private ZoneId timezone;

    @Size(max = 127, message = "The place must be at most 127 characters long")
    private String place;
}
