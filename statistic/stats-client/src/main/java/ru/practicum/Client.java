package ru.practicum;

import dto.EndpointHit;
import dto.ViewStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class Client extends BaseClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${stats.api.url:http://localhost:9090}")
    private String url;

    public Client(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public void hit(EndpointHit endpointHit) {
        post(url + "/hit", endpointHit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/stats")
                .queryParam("start", encodeDateTime(start))
                .queryParam("end", encodeDateTime(end))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                uriBuilder.queryParam("uris", uri);
            }
        }
        return (List<ViewStats>) get(uriBuilder.toUriString()).getBody();
    }

    private String encodeDateTime(LocalDateTime dateTime) {
        String formatted = dateTime.format(FORMATTER);
        return URLEncoder.encode(formatted, StandardCharsets.UTF_8);
    }
}