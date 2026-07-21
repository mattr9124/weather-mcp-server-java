package dev.chiedo.weathermcpserver.service;

import dev.chiedo.weathermcpserver.model.WeatherDataResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherServiceTest {

    @Test
    void getWeatherDetailsByLocationWorks() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://wttr.in/");

        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        WeatherService weatherService = new WeatherService(restClient, new ObjectMapper());

        String url = "https://wttr.in/Nairobi?format=j1";

        String sampleResponse = """
                {
                  "current_condition": [
                    {
                      "temp_C": "25",
                      "temp_F": "77",
                      "humidity": "51",
                      "windspeedKmph": "9",
                      "weatherDesc": [
                        { "value": "Partly cloudy" }
                      ]
                    }
                  ]
                }
                """;

        server.expect(requestTo(url))
                .andRespond(withSuccess(sampleResponse, MediaType.APPLICATION_JSON));

        WeatherDataResponse response = weatherService.getWeatherDetailsByLocation("Nairobi");

        assertThat(response.location()).isEqualTo("Nairobi");
        assertThat(response.tempCelsius()).isEqualTo("25");
        assertThat(response.tempFahrenheit()).isEqualTo("77");
        assertThat(response.humidity()).isEqualTo("51");
        assertThat(response.windSpeed()).isEqualTo("9");
        assertThat(response.weatherDescription()).isEqualTo("Partly cloudy");

        server.verify();
    }
}
