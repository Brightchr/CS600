package com.snhu.mscs.cs600.module3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityConfigTests -
 *
 * Purpose:
 *      Verifies the authentication and authorization rules
 *      configured for the Module Three application.
 *
 * Expected Results:
 *      The version endpoint remains public.
 *      The books endpoint requires valid authentication.
 *
 * Version              Author                  Date            Description
 * ***********************************************************************
 *     1                 Christopher Bright      07-Oct-2026     Initial Creation
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

  @Autowired
  private MockMvc mockMvc;

  /**
   * Verifies that the version endpoint remains publicly accessible.
   */
  @Test
  void versionEndpointAllowsUnauthenticatedAccess() throws Exception {

    mockMvc.perform(
            get("/api/v1/version")
        )
        .andExpect(status().isOk());
  }

  /**
   * Verifies that the books endpoint rejects requests
   * that do not contain authentication credentials.
   */
  @Test
  void booksEndpointRejectsUnauthenticatedAccess() throws Exception {

    mockMvc.perform(
            get("/api/v1/books")
        )
        .andExpect(status().isUnauthorized());
  }

  /**
   * Verifies that invalid credentials cannot access
   * the protected books endpoint.
   */
  @Test
  void booksEndpointRejectsInvalidCredentials() throws Exception {

    mockMvc.perform(
            get("/api/v1/books")
                .with(httpBasic(
                    "namedUser",
                    "wrongPassword"
                ))
        )
        .andExpect(status().isUnauthorized());
  }

  /**
   * Verifies that the configured user can access
   * the protected books endpoint.
   */
  @Test
  void booksEndpointAllowsAuthenticatedAccess() throws Exception {

    mockMvc.perform(
            get("/api/v1/books")
                .with(httpBasic(
                    "namedUser",
                    "names.123"
                ))
        )
        .andExpect(status().isOk());
  }
}