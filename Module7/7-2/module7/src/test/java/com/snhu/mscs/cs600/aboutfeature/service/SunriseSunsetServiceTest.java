
package com.snhu.mscs.cs600.aboutfeature.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

/**
 * Tests API response processing and error handling using mock requests.
 *
 * Author: Chris Bright
 * Date: 09-Nov-2026
 */
class SunriseSunsetServiceTest {

  private RestTemplate restTemplate;
  private SunriseSunsetService service;

  /**
   * Creates a mock HTTP client before each test.
   */
  @BeforeEach
  void setUp() {

    restTemplate = mock(RestTemplate.class);
    service = new SunriseSunsetService(restTemplate);
  }

  /**
   * Verifies that a successful response is processed correctly.
   */
  @Test
  void testSuccessfulResponse() {

    String response = """
                {
                    "status": "OK",
                    "results": {
                        "sunrise": "6:39:52 AM",
                        "sunset": "5:03:16 PM"
                    }
                }
                """;

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(response);

    SunriseSunsetService.SunTimes result =
        service.getSunriseAndSet(38.88941, -77.05013);

    assertEquals("6:39:52 AM", result.sunrise());
    assertEquals("5:03:16 PM", result.sunset());
    assertTrue(result.isAvailable());
  }

  /**
   * Verifies that custom coordinates are included in the API request.
   */
  @Test
  void testOtherCoordinates() {

    String response = """
                {
                    "status": "OK",
                    "results": {
                        "sunrise": "7:00 AM",
                        "sunset": "5:00 PM"
                    }
                }
                """;

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(response);

    service.getSunriseAndSet(47.62061, -122.34926);

    verify(restTemplate).getForObject(
        "https://api.sunrisesunset.io/json?lat=47.62061&lng=-122.34926",
        String.class
    );
  }

  /**
   * Verifies that connection failures are handled.
   */
  @Test
  void testConnectionFailure() {

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenThrow(new ResourceAccessException("Connection failed"));

    assertFalse(
        service.getSunriseAndSet(38.88941, -77.05013).isAvailable()
    );
  }

  /**
   * Verifies that empty API responses are handled.
   */
  @Test
  void testEmptyResponse() {

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn("");

    assertFalse(
        service.getSunriseAndSet(38.88941, -77.05013).isAvailable()
    );
  }

  /**
   * Verifies that an unsuccessful API status is handled.
   */
  @Test
  void testUnsuccessfulStatus() {

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn("{\"status\":\"ERROR\",\"results\":{}}");

    assertFalse(
        service.getSunriseAndSet(38.88941, -77.05013).isAvailable()
    );
  }

  /**
   * Verifies that missing sunset information is handled.
   */
  @Test
  void testMissingSunset() {

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(
            "{\"status\":\"OK\",\"results\":{\"sunrise\":\"6:39 AM\"}}"
        );

    assertFalse(
        service.getSunriseAndSet(38.88941, -77.05013).isAvailable()
    );
  }

  /**
   * Verifies that invalid JSON does not break the application.
   */
  @Test
  void testInvalidJson() {

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn("{invalid json");

    assertFalse(
        service.getSunriseAndSet(38.88941, -77.05013).isAvailable()
    );
  }

  /**
   * Verifies that invalid coordinates do not generate an HTTP request.
   */
  @Test
  void testInvalidCoordinates() {

    SunriseSunsetService.SunTimes result =
        service.getSunriseAndSet(100, -77.05013);

    assertFalse(result.isAvailable());
    verifyNoInteractions(restTemplate);
  }
}
