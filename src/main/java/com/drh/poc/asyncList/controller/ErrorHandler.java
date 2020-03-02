package com.drh.poc.asyncList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandler {

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<String> test(RuntimeException e) {
    System.out.println("ERROR HANDLER: " + e.getClass());
    return ResponseEntity.status(400).body(e.getMessage());
  }
}
