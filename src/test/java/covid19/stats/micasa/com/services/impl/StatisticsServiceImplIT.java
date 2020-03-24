package covid19.stats.micasa.com.services.impl;

import covid19.stats.micasa.com.domain.Location;
import covid19.stats.micasa.com.domain.Reading;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@DisplayName("Stats Service")
public class StatisticsServiceImplIT {

    @Test
    @DisplayName("Fetch from Johns Hopkins CSSE repository and map to internal format")
    public void map() throws IOException {

        StatsServiceImpl service = new StatsServiceImpl();
        var stats = service.retrieveStats().join();

        assertThat("There are 463 locations", stats.keySet().size(), is(482));

    }

}
