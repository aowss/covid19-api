package covid19.stats.micasa.com.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Reading")
public class ReadingTest {

    @Test
    @DisplayName("reading comparison : country takes precedence")
    public void differentCountry() {
        var reading1 = new Reading(new Location("Canada"), LocalDate.of(2020, 2, 28),new Statistic(5, 1, 1));
        var reading2 = new Reading(new Location("Italy"), LocalDate.of(2020, 1, 28),new Statistic(5, 1, 1));
        assertThat(reading1.compareTo(reading2), is(lessThan(0)));
    }

    @Test
    @DisplayName("reading comparison : date is used if country is the same")
    public void differentDate() {
        var reading1 = new Reading(new Location("Canada"), LocalDate.of(2020, 2, 28),new Statistic(5, 1, 1));
        var reading2 = new Reading(new Location("Canada"), LocalDate.of(2020, 1, 28),new Statistic(5, 1, 1));
        assertThat(reading1.compareTo(reading2), is(greaterThan(0)));
    }

}
