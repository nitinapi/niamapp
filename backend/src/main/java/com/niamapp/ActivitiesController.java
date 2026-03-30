package com.niamapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niamapp.proto.ActivitiesProto;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ActivitiesController {

    // Curated pool of toddler-friendly Seattle-area venues.
    // Nominatim is queried live so names/addresses are always up to date.
    private static final List<String> VENUE_QUERIES = List.of(
            "Seattle Children's Museum Seattle",
            "Woodland Park Zoo Seattle",
            "Seattle Aquarium",
            "Pacific Science Center Seattle",
            "Discovery Park Seattle",
            "Green Lake Park Seattle",
            "Alki Beach Park Seattle",
            "Carkeek Park Seattle",
            "Museum of Flight Seattle",
            "Remlinger Farms Carnation Washington",
            "Kelsey Creek Farm Park Bellevue",
            "Mercer Slough Nature Park Bellevue"
    );

    private static final String NOMINATIM_BASE =
            "https://nominatim.openstreetmap.org/search?format=json&limit=1&addressdetails=1&q=";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/activities", produces = "application/x-protobuf")
    public byte[] getActivities() throws Exception {
        List<String> shuffled = new ArrayList<>(VENUE_QUERIES);
        Collections.shuffle(shuffled);

        List<ActivitiesProto.Activity> activities = new ArrayList<>();

        for (String query : shuffled) {
            if (activities.size() >= 2) break;

            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NOMINATIM_BASE + encoded))
                    .header("User-Agent", "NiamApp/1.0")
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode results = objectMapper.readTree(response.body());

            if (results.isArray() && results.size() > 0) {
                JsonNode place = results.get(0);

                String name = place.path("name").asText();
                if (name.isBlank()) continue;

                String description = buildDescription(place.path("address"));
                String mapsUrl = "https://maps.google.com/maps?q="
                        + place.path("lat").asText() + ","
                        + place.path("lon").asText();

                activities.add(ActivitiesProto.Activity.newBuilder()
                        .setName(name)
                        .setDescription(description)
                        .setUrl(mapsUrl)
                        .build());
            }

            // Nominatim usage policy: max 1 request per second
            Thread.sleep(1100);
        }

        return ActivitiesProto.ActivitiesResponse.newBuilder()
                .addAllActivities(activities)
                .build()
                .toByteArray();
    }

    private String buildDescription(JsonNode address) {
        List<String> parts = new ArrayList<>();
        for (String field : new String[]{"road", "suburb", "neighbourhood", "city", "town", "state"}) {
            String val = address.path(field).asText();
            if (!val.isBlank()) parts.add(val);
            if (parts.size() == 3) break;
        }
        return String.join(", ", parts);
    }
}
