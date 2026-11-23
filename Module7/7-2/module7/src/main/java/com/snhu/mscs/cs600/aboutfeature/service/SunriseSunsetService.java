
package com.snhu.mscs.cs600.aboutfeature.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * SunriseSunsetService - Communicates with the external Sunrise/Sunset API.
 *
 * Keeps HTTP requests, response parsing and error handling out of the View.
 * The caller supplies coordinates so the service works for any location.
 *
 * Author: Chris Bright
 * Date: 09-Nov-2026
 */
@Service
public class SunriseSunsetService {

  private static final Logger logger =
      LoggerFactory.getLogger(SunriseSunsetService.class);

  private static final String API_URL =
      "https://api.sunrisesunset.io/json";

  private final RestTemplate restTemplate;

  /**
   * Configures the HTTP client used by the application.
   */
  public SunriseSunsetService() {
    this(createRestTemplate());
  }

  /**
   * Allows tests to provide a mock HTTP client.
   */
  SunriseSunsetService(RestTemplate restTemplate) {
    this.restTemplate = Objects.requireNonNull(restTemplate);
  }

  /**
   * Creates an HTTP client with connection and read timeouts.
   */
  private static RestTemplate createRestTemplate() {

    SimpleClientHttpRequestFactory factory =
        new SimpleClientHttpRequestFactory();

    factory.setConnectTimeout(5000);
    factory.setReadTimeout(5000);

    return new RestTemplate(factory);
  }

  /**
   * Requests and validates sunrise and sunset data.
   *
   * @param latitude Latitude between -90 and 90.
   * @param longitude Longitude between -180 and 180.
   * @return Sunrise and sunset times, or unavailable values.
   */
  public SunTimes getSunriseAndSet(double latitude, double longitude) {

    // Reject coordinates outside the valid geographic range.
    if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
        || latitude < -90 || latitude > 90
        || longitude < -180 || longitude > 180) {

      logger.error("Invalid coordinates: latitude={}, longitude={}",
          latitude, longitude);

      return SunTimes.unavailable();
    }

    // Construct the URL using the requested coordinates.
    String url = String.format(Locale.US, "%s?lat=%s&lng=%s",
        API_URL, latitude, longitude);

    try {

      // Make the HTTP request to the external API.
      String response =
          restTemplate.getForObject(url, String.class);

      // Verify that the response contains data.
      if (response == null || response.isBlank()) {
        throw new IllegalStateException(
            "The API returned an empty response.");
      }

      // Convert the JSON response into a Java Map.
      JsonParser parser = JsonParserFactory.getJsonParser();
      Map<String, Object> data = parser.parseMap(response);

      // Confirm that the API request was successful.
      if (!"OK".equals(data.get("status"))) {
        throw new IllegalStateException(
            "The API did not return an OK status.");
      }

      // Extract the nested results object.
      if (!(data.get("results") instanceof Map<?, ?> results)) {
        throw new IllegalStateException(
            "The API response is missing results.");
      }

      // Retrieve sunrise and sunset values.
      Object sunriseValue = results.get("sunrise");
      Object sunsetValue = results.get("sunset");

      // Verify that both values are valid strings.
      if (!(sunriseValue instanceof String sunrise)
          || sunrise.isBlank()
          || !(sunsetValue instanceof String sunset)
          || sunset.isBlank()) {

        throw new IllegalStateException(
            "Sunrise or sunset information is missing.");
      }

      return new SunTimes(sunrise, sunset);

    } catch (RestClientException | IllegalArgumentException
             | IllegalStateException e) {

      // Log unexpected API failures as errors.
      logger.error(
          "Sunrise/Sunset API failed for latitude={}, longitude={}",
          latitude, longitude, e);

      // Return fallback values so the application remains available.
      return SunTimes.unavailable();
    }
  }

  /**
   * Stores the sunrise and sunset values returned by the API.
   */
  public record SunTimes(String sunrise, String sunset) {

    public boolean isAvailable() {
      return !"Unavailable".equals(sunrise)
          && !"Unavailable".equals(sunset);
    }

    private static SunTimes unavailable() {
      return new SunTimes("Unavailable", "Unavailable");
    }
  }
}
