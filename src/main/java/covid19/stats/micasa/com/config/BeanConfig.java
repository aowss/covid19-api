package covid19.stats.micasa.com.config;

import covid19.stats.micasa.com.activities.*;
import covid19.stats.micasa.com.repositories.StatisticsRepository;
import covid19.stats.micasa.com.services.LocationsService;
import covid19.stats.micasa.com.services.StatisticsService;
import covid19.stats.micasa.com.services.impl.JohnsHopkinsStatisticsServiceImpl;

import covid19.stats.micasa.com.services.impl.ResourcesLocationsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class BeanConfig {

    @Value("${data.jhu.time-series.confirmed}") String confirmedUrl;
    @Value("${data.jhu.time-series.deaths}") String deathsUrl;
    @Value("${data.jhu.time-series.recovered}") String recoveredUrl;

    @Bean
    public RetrieveStatistics retrieveStatistics(StatisticsRepository repository) {
        return new RetrieveStatistics(repository);
    }

    @Bean
    public RetrieveMetadata retrieveMetadata(StatisticsRepository repository) {
        return new RetrieveMetadata(repository);
    }

    @Bean
    public LoadStatistics loadStatistics(StatisticsRepository repository, StatisticsService statisticsService, LocationsService locationsService) {
        return new LoadStatistics(repository, statisticsService, locationsService);
    }

    @Bean
    public StatisticsRepository statisticsRepository() {
        return new StatisticsRepository();
    }

    @Bean
    public StatisticsService statsService() {
        return new JohnsHopkinsStatisticsServiceImpl(confirmedUrl, deathsUrl, recoveredUrl);
    }

    @Bean
    public LocationsService locationsService() {
        return new ResourcesLocationsServiceImpl();
    }

}
