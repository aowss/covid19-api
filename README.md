# COVID-19 API

I was inspired to create this by Nate Murray's email : `The Code of COVID-19 - Open-source projects to understand - and fight - the global pandemic`.

Some good resources are :

* [Johns Hopkins CSSE's dashboard](https://coronavirus.jhu.edu/map.html)
* [Open COVID-19 Data](https://github.com/open-covid-19/data)
* [Bloomberg's Dashboard](https://www.bloomberg.com/graphics/2020-coronavirus-cases-world-map/?utm_source=facebook&utm_medium=cpc&utm_campaign=covid19&utm_content=tofu&fbclid=IwAR0UgjaNWvouJAPggGdA6VnogRuCM2SPWePRKYcfSeF3coYcgqS5DepTYXw)
* [Le Monde analysis in French](https://www.lemonde.fr/les-decodeurs/article/2020/02/27/en-carte-visualisez-la-propagation-mondiale-de-l-epidemie-de-coronavirus_6031092_4355770.html)
* [The Guardian's analysis](https://www.theguardian.com/world/2020/mar/27/coronavirus-mapped-map-which-countries-have-the-most-cases-and-deaths)
* [Worldometer's summary](https://www.worldometers.info/coronavirus/)
* [Ontario's details](https://www.ontario.ca/page/2019-novel-coronavirus?utm_source=Google&utm_medium=CPC&utm_campaign=COVID-19)
* [Another dashboard](https://www.gohkokhan.com/corona-virus-interactive-dashboard-tweaked/)
* [Configurable visualiztion](http://91-divoc.com/pages/covid-visualization/)
* [Predictions notebook](https://www.kaggle.com/yuanquan/covid-19-prediction-by-country-and-province)
* [Video of the evolution over time](https://prateekiiest.github.io/COVID-19-Analysis/)
* [Sources comparison](https://ourworldindata.org/covid-sources-comparison)
* [Trends](https://aatishb.com/covidtrends/)
* [Le Monde trend Analysis in French](https://www.lemonde.fr/les-decodeurs/article/2020/03/27/coronavirus-visualisez-les-pays-qui-ont-aplati-la-courbe-de-l-infection-et-ceux-qui-n-y-sont-pas-encore-parvenus_6034627_4355770.html)
* [Data Scraper](https://coronadatascraper.com/#home)
* [API](https://corona-api-landingpage.netlify.com/)
* [Different Data Sets](https://github.com/cipriancraciun/covid19-datasets)
* [European Centre for Disease Prevention and Control](https://www.ecdc.europa.eu/en/publications-data/download-todays-data-geographic-distribution-covid-19-cases-worldwide) 's [Data in JSON format](https://opendata.ecdc.europa.eu/covid19/casedistribution/json/)

## Data Source

The data is sourced from [Johns Hopkins CSSE's COVID-19 Data Repository](https://github.com/CSSEGISandData/COVID-19).  

The problem with this source is that the format keeps changing and is inconsistent :
* the recovered cases are not reported anymore
* file names change
* some files are, at some point in time, not well-formed
* some countries that are broken down in regions have a summary in the file, e.g. `France`, and some don't, e.g. `Canada`
* `Canada` has an entry where the region is named `Recovered` !
* for some reason, `US` has a separate file

Other sources might be worth considering :
* https://github.com/cipriancraciun/covid19-datasets

### Validation

The country name and the region name is validated against https://open-covid-19.github.io/data/metadata.csv.

## Implementation

### Java & Spring versions

* This project uses Java 14 and the latest [Spring Boot](https://spring.io/projects/spring-boot) version.

* Version `3.0.0-M4` of the `surefire` plugin is needed to run the tests.

### Flow

The data is reloaded in memory, in the [`StatisticsRepository`](./src/main/java/covid19/stats/micasa/com/repositories/StatisticsRepository.java) class, from the source, by the [`LoadStatistics`](./src/main/java/covid19/stats/micasa/com/activities/LoadStatistics.java) class, on a fixed schedule.  
The source location and the reload frequency are configured in the [application's config file](./src/main/resources/application.yaml).

Because the reload of the entire data set is fast, we use the [full data sets](https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series) and not the [daily delta sets](https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_daily_reports).  
This simplifies the logic and allows for corrections to past data in case this happens.

## Run

### Command Line

__From the root of the project__ :

* Use Java 14

> `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-14.jdk/Contents/Home`

* Build and package

> `mvn package`

* Start the service

> `java --enable-preview -jar target/covid-19-api.jar`

The server port is `9000` as specified in the [application configuration](./src/main/resources/application.yaml).

* Access the API

> `curl -X GET 'http://localhost:9000/covid19/stats?location=US&from=2020-02-01&to=2020-02-29'`

```json
{
    "US / Sullivan, TN": [
        {
            "date": "2020-02-01",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        },
        ...
        {
            "date": "2020-02-28",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        }
    ],
    ...
    "US / Honolulu County, HI": [
        {
            "date": "2020-02-01",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        },
        ...
        {
            "date": "2020-02-28",
            "value": {
                "confirmedCases": 0,
                "deaths": 0,
                "recoveries": 0
            }
        }
    ]
}
```

> `curl -X GET 'http://localhost:9000/covid19/info'`

```json
{
    "lastUpdateTime": "2020-03-23T22:40:14.272977Z",
    "from": "2020-01-22",
    "to": "2020-03-22",
    "locations": [
        {
            "country": "US",
            "province": "Bon Homme, SD"
        },
        ...
        {
            "country": "Lebanon",
            "province": ""
        },
        {
            "country": "China",
            "province": "Macau"
        }
    ]
}
```

* Debug

If you need to debug the service, use the following command to start the service

> `java --enable-preview -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n -jar target/covid-19-api.jar`

then connect a remote debugging session to port `8000`


