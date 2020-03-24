package covid19.stats.micasa.com.activities;

import covid19.stats.micasa.com.repositories.StatisticsRepository;
import covid19.stats.micasa.com.rest.representations.MetadataRepresentation;

public class RetrieveMetadata {

    StatisticsRepository statisticsRepository;

    public RetrieveMetadata(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public MetadataRepresentation retrieveMetadata() {
        return new MetadataRepresentation(statisticsRepository.getLastUpdateTime(), statisticsRepository.getFrom(), statisticsRepository.getTo(), statisticsRepository.getStatistics().keySet());
    }

}
