package com.sinyoung.command;

import com.google.gson.Gson;
import com.sinyoung.enums.PhoneStatus;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;


@Slf4j
@Component
// @PropertySource("classpath:com.sinyoung.command.properties")
public class ManagementCommand {

    @Value("${command.management.baseUrl}")
    private String baseUrl;

    @Value("${command.management.uri}")
    private String uri;

    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(conn -> conn
                    .addHandlerLast(new ReadTimeoutHandler(10))
                    .addHandlerLast(new WriteTimeoutHandler(10))));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient.wiretap(true));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(connector)
                .build();
    }

    private ResponseEntity<String> request(HttpMethod method, String path, LinkedMultiValueMap<String, String> map) {
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            return ResponseEntity.ok(webClient()
                    .method(method)
                    .uri(uri + path)
                    .body(BodyInserters.fromFormData(map))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(10_000))
                    .retry(3)
                    .block());
        }
        else {
            return ResponseEntity.ok(webClient()
                    .method(method)
                    .uri(builder -> builder.path(uri + path).queryParams(map).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(10_000))
                    .retry(3)
                    .block());
        }
    }

    public String setConfig(String macAddr, Map<String, Object> inputConfigData, PhoneStatus phoneStatus) {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        Gson gson = new Gson();
        map.add("configData", gson.toJson(inputConfigData));
        map.add("macAddr", macAddr);
        if (phoneStatus != null) {
            map.add("phoneStatus", phoneStatus.name());
        }
        ResponseEntity<String> response = request(HttpMethod.PUT, "/config", map);
        return response.getBody();
    }

    public String updateStatus(String macAddr, PhoneStatus phoneStatus) {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        Gson gson = new Gson();
        map.add("macAddr", macAddr);
        map.add("phoneStatus", phoneStatus.name());
        ResponseEntity<String> response = request(HttpMethod.PUT, "/status", map);
        return response.getBody();
    }

    public String init(String macAddr) {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("macAddr", macAddr);
        ResponseEntity<String> response = request(HttpMethod.PUT, "/init", map);
        return response.getBody();
    }
}
