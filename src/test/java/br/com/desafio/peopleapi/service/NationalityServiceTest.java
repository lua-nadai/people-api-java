package br.com.desafio.peopleapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NationalityServiceTest {
    @Test
    void returnsPortugueseCountryNameFromRankedResponse() {
        var builder = RestClient.builder().baseUrl("https://api.nationalize.io");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new NationalityService(builder.build(), "test-key");

        server.expect(requestTo("https://api.nationalize.io?name=Jos%C3%A9%20Maria&apikey=test-key"))
            .andRespond(withSuccess("""
                {"country":[{"country_id":"BR","probability":0.7},{"country_id":"PT","probability":0.2}]}
                """, MediaType.APPLICATION_JSON));

        var result = service.predict("José Maria");
        assertEquals("BR", result.countryCode());
        assertEquals("Brasil", result.nationality());
        server.verify();
    }
}
