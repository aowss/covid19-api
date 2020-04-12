package covid19.stats.micasa.com.services.impl;

import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DisplayName("Stats Service")
public class StatisticsServiceImplIT {

    @Test
    @DisplayName("Fetch from Johns Hopkins CSSE repository and map to internal format")
    public void map(){

        StatsServiceImpl service = new StatsServiceImpl();
        var stats = service.retrieveStats().join();

        assertThat("There are 463 locations", stats.keySet().size(), is(482));

    }

}
