package com.role.net.gogather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PlacesApiService {

    private final RestClient restClient;

    @Value("${google.places.api-key}")
    private String apiKey;

    public PlacesApiService() {
        this.restClient = RestClient.create();
    }

    public String searchPlaces(String query) {
        try {
            String requestBody = "{ \"textQuery\": \"" + query + "\" }";

            return restClient.post()
                    .uri("https://places.googleapis.com/v1/places:searchText")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("Content-Type", "application/json")
                    // Filtramos apenas os campos úteis para economizar tokens da IA
                    .header("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.rating,places.priceLevel")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            return "Não foi possível encontrar locais no momento. Erro: " + e.getMessage();
        }
    }

    public JsonNode getPlaceDetails(String placeId) {
        try {
            String responseStr = restClient.get()
                    .uri("https://places.googleapis.com/v1/places/" + placeId)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "id,displayName,formattedAddress,location,addressComponents")
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(responseStr);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao buscar detalhes do local no Google: " + e.getMessage());
        }
    }
}
