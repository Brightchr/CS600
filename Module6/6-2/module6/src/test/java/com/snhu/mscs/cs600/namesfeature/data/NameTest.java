
package com.snhu.mscs.cs600.namesfeature.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests email generation for the Module Six feature.
 * Author: Chris Bright
 */
class NameTest {

  @Test
  void generatesEmailInRequiredFormat() {
    Name name = new Name();
    name.setFirst("John");
    name.setLast("Smith");

    assertEquals("john.smith@example.com", name.getEmail());
  }

  @Test
  void convertsNamesToLowercase() {
    Name name = new Name();
    name.setFirst("MARY");
    name.setLast("JOHNSON");

    assertEquals("mary.johnson@example.com", name.getEmail());
  }

  @Test
  void excludesMiddleNameAndSuffix() {
    Name name = new Name();
    name.setFirst("Sarah");
    name.setMiddle("Ann");
    name.setLast("Brown");
    name.setSuffix("Jr.");

    assertEquals("sarah.brown@example.com", name.getEmail());
  }
}
