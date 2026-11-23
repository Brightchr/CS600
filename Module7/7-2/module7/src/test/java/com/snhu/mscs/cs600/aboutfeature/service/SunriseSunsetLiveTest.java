
package com.snhu.mscs.cs600.aboutfeature.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the application using real Sunrise/Sunset API requests.
 *
 * These tests require an internet connection.
 *
 * Author: Chris Bright
 * Date: 09-Nov-2026
 */
class SunriseSunsetLiveTest {

  /**
   * Tests a live API request for the Lincoln Memorial.
   */
  @Test
  @DisabledIfEnvironmentVariable(
      named = "SKIP_LIVE_API_TESTS",
      matches = "true"
  )
  void testLiveLincolnMemorial() {

    SunriseSunsetService service = new SunriseSunsetService();

    SunriseSunsetService.SunTimes times =
        service.getSunriseAndSet(38.88941, -77.05013);

    assertTrue(
        times.isAvailable(),
        "The API should return valid sunrise and sunset times"
    );

    assertFalse(times.sunrise().isBlank());
    assertFalse(times.sunset().isBlank());
  }

  /**
   * Tests a live API request using a different location.
   */
  @Test
  @DisabledIfEnvironmentVariable(
      named = "SKIP_LIVE_API_TESTS",
      matches = "true"
  )
  void testLiveSpaceNeedle() {

    SunriseSunsetService service = new SunriseSunsetService();

    SunriseSunsetService.SunTimes times =
        service.getSunriseAndSet(47.62061, -122.34926);

    assertTrue(
        times.isAvailable(),
        "The API should support different geographic locations"
    );

    assertFalse(times.sunrise().isBlank());
    assertFalse(times.sunset().isBlank());
  }
}
