package covid19.stats.micasa.com.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DisplayName("Statistic")
public class StatisticTest {

    @Test
    @DisplayName("add statistics")
    public void add() {
        var stat1 = new Statistic(5, 1, 1);
        var stat2 = new Statistic(10, 0, 2);
        assertThat(stat1.add(stat2), is (new Statistic(15, 1, 3)));
    }

}
