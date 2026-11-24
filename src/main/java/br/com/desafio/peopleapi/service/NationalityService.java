package br.com.desafio.peopleapi.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Service
public class NationalityService {
    private static final Locale PORTUGUESE = Locale.forLanguageTag("pt-BR");
    private final RestClient client;
    private final String apiKey;

    @Autowired
    public NationalityService(@Value("${app.nationalize.api-key}") String apiKey) {
        this(RestClient.builder().baseUrl("https://api.nationalize.io").requestFactory(requestFactory()).build(), apiKey);
    }

    NationalityService(RestClient client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    private static SimpleClientHttpRequestFactory requestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;

    }

    public NationalityResult predict(String name) {
        NationalizeResponse response = client.get()
            .uri(uri -> {
                uri.queryParam("name", "{name}");
                if (apiKey.isBlank()) {
                    return uri.build(name);
                }
                return uri.queryParam("apikey", "{key}").build(name, apiKey);
            })
            .retrieve().body(NationalizeResponse.class);

        if (response == null || response.country() == null || response.country().isEmpty()) {
            return new NationalityResult(name, null, "Não foi possível estimar a nacionalidade");
        }

        String code = response.country().get(0).countryId();
        if (code == null || !Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).contains(code)) {
            throw new org.springframework.web.client.RestClientException("Código ISO inválido no Nationalize");
        }
        String country = new Locale.Builder().setRegion(code).build().getDisplayCountry(PORTUGUESE);
        return new NationalityResult(name, code, country);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NationalizeResponse(List<CountryPrediction> country) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountryPrediction(@JsonProperty("country_id") String countryId) {}

    public record NationalityResult(String name, String countryCode, String nationality) {}
}
