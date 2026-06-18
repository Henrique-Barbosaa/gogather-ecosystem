package br.com.gogather.framework.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.role.net.gogather.service.PlacesApiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class AiToolsConfig {

    public record PlaceSearchRequest(
            @JsonPropertyDescription("A string de pesquisa livre do usuário para encontrar um local ou tipo de estabelecimento. Ex: 'Bares em Candelária', 'Pizzaria em Campim Macio', 'Bosque dos Namorados', 'Onde tem um bom café perto de mim?'")
            String query
    ) {}

    @Bean
    @Description("Busca locais reais, estabelecimentos comerciais, restaurantes e pontos de interesse usando a API do Google Places. Acione esta ferramenta SEMPRE que o usuário pedir sugestões de onde ir, informações sobre um lugar, ou ideias de roteiro.")
    public Function<PlaceSearchRequest, String> searchPlacesTool(PlacesApiService placesApiService) {

        return request -> {
            System.out.println("IA acionou a ferramenta de busca de locais com a query: " + request.query());
            return placesApiService.searchPlaces(request.query());
        };
    }
}
