package com.garemobilegb.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BoardingScanSyncResultItem(
    long clientLogId,
    String status,
    String errorCode,
    String errorMessage) {

  public static BoardingScanSyncResultItem synced(long clientLogId) {
    return new BoardingScanSyncResultItem(clientLogId, "SYNCED", null, null);
  }

  public static BoardingScanSyncResultItem error(
      long clientLogId, String errorCode, String errorMessage) {
    return new BoardingScanSyncResultItem(clientLogId, "ERROR", errorCode, errorMessage);
  }
}
