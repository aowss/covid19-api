package covid19.stats.micasa.com.services;

import covid19.stats.micasa.com.domain.Location;

import java.util.Set;

public interface LocationsService {

    Set<Location> loadLocations();

}
