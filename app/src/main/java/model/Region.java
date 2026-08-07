package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Region {

    @JsonProperty("region_code")
    private long regionCode;
    @JsonProperty("name")
    private String regionName;
}
