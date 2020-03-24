package covid19.stats.micasa.com.config;

import covid19.stats.micasa.com.activities.*;
import covid19.stats.micasa.com.repositories.StatisticsRepository;
import covid19.stats.micasa.com.services.StatisticsService;
import covid19.stats.micasa.com.services.impl.JHStatisticsServiceImpl;

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
    public LoadStatistics loadStatistics(StatisticsRepository repository, StatisticsService service) {
        return new LoadStatistics(repository, service);
    }

    @Bean
    public StatisticsRepository statisticsRepository() {
        return new StatisticsRepository();
    }

    @Bean
    public StatisticsService statsService() {
        return new JHStatisticsServiceImpl(confirmedUrl, deathsUrl, recoveredUrl);
    }

}
