package com.breskul.bibernate;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.breskul.bibernate.demo.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class MainTest {

  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @BeforeEach
  public void setUp() {
    System.setOut(new PrintStream(outputStreamCaptor));
  }

  @Test
  @Order(1)
  @DisplayName("Check that Hello Bibernate message is appeared in System.out")
  public void given_Main_when_CallMain_then_showHelloMessage() {
    // when
    Main.main(new String[]{});

    // then
    Assertions.assertEquals("Hello, Bibernate!", outputStreamCaptor.toString()
        .trim());
  }
}
