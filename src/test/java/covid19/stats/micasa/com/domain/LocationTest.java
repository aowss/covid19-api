package covid19.stats.micasa.com.domain;

import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Location")
public class LocationTest {

    @Test
    @DisplayName("location comparison : country takes precedence")
    public void differentCountry() {
        var location1 = new Location("Canada", "Quebec");
        var location2 = new Location("France", "Martinique");
        assertThat(location1.compareTo(location2), is(lessThan(0)));
    }

    @Test
    @DisplayName("location comparison : region is used if country is the same")
    public void differentRegion() {
        var location1 = new Location("Canada", "Quebec");
        var location2 = new Location("Canada", "Ontario");
        assertThat(location1.compareTo(location2), is(greaterThan(0)));
    }

    @Test
    @DisplayName("location comparison : no region comes before")
    public void noRegion() {
        var location1 = new Location("Canada");
        var location2 = new Location("Canada", "Ontario");
        assertThat(location1.compareTo(location2), is(lessThan(0)));
    }

    @Test
    @DisplayName("location equality : coordinates are not taken into account")
    public void equality() {
        var location1 = new Location("Canada", "Ontario", 51.2538f,-85.3232f);
        var location2 = new Location("Canada", "Ontario", 51f,-85f);
        assertThat(location1.equals(location2), is(true));
    }

}
