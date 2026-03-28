package com.groupc.shared.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

/**
 * Klientklass för att interagera med en extern valutatjänst.
 * Denna klass tillhandahåller funktionalitet för att hämta växelkurser mellan olika valutor.
 * Den använder {@link HttpClient} med virtuella trådar (Project Loom) för att utföra icke-blockerande
 * nätverksanrop, vilket förbättrar prestanda och skalbarhet för I/O-bundna operationer.
 *
 * <p>Konstruktorn {@link #CurrencyClient(String)} tar emot bas-URL:en för valutatjänsten.
 * Alla anrop till valutatjänsten kommer att prefixas med denna URL.
 *
 * <p>Exempel på användning:
 * <pre>{@code
 * CurrencyClient client = new CurrencyClient("http://localhost:8080");
 * try {
 *     double rate = client.getExchangeRate("USD", "EUR");
 *     System.out.println("Växelkurs USD till EUR: " + rate);
 * } catch (RuntimeException e) {
 *     System.err.println("Kunde inte hämta växelkurs: " + e.getMessage());
 * }
 * }</pre>
 */
public class CurrencyClient {

    private static final Logger log = LoggerFactory.getLogger(CurrencyClient.class);
    private final HttpClient httpClient;
    private final String currencyServiceUrl;

    public CurrencyClient(String currencyServiceUrl) {
        this.currencyServiceUrl = currencyServiceUrl;
        this.httpClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

    public double getExchangeRate(String fromCurrency, String toCurrency) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(currencyServiceUrl + "/api/v1/currency/convert?base=" + fromCurrency + "&target=" + toCurrency))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Double.parseDouble(response.body());
            } else {
                log.error("Misslyckades att hämta kurs. Status: {}", response.statusCode());
                return 1.0;
            }
        } catch (Exception e) {
            log.error("Nätverksfel mot valutatjänst", e);
            throw new RuntimeException("Currency service unavailable", e);
        }
    }
}