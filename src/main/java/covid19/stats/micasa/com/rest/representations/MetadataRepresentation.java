package covid19.stats.micasa.com.rest.representations;

import com.fasterxml.jackson.annotation.JsonProperty;
import covid19.stats.micasa.com.domain.Location;

import java.time.*;
import java.util.Set;

public record MetadataRepresentation(@JsonProperty ZonedDateTime lastUpdateTime, @JsonProperty LocalDate from, @JsonProperty LocalDate to, @JsonProperty Set<Location> locations) {
}
