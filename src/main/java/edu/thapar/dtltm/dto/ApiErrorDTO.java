package edu.thapar.dtltm.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ApiErrorDTO {
  private LocalDateTime timeStamp;
  private int status;
  private String error;
  private String message;
  private String path;

  public ApiErrorDTO(int status, String error, String message, String path) {
    this.timeStamp = LocalDateTime.now();
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }
}
