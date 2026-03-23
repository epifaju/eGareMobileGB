package com.garemobilegb.shared.exceptions;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> fieldErrors) {

  public ApiError(String code, String message) {
    this(code, message, null);
  }
}
