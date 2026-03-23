package com.garemobilegb.vehicle.domain;

public enum VehicleSeatLayout {
  L8(8, 2, 2, 2),
  L15(15, 5, 2, 1),
  L20(20, 5, 2, 2),
  L45(45, 9, 2, 3);

  private final int capacity;
  private final int rows;
  private final int leftSeatsPerRow;
  private final int rightSeatsPerRow;

  VehicleSeatLayout(int capacity, int rows, int leftSeatsPerRow, int rightSeatsPerRow) {
    this.capacity = capacity;
    this.rows = rows;
    this.leftSeatsPerRow = leftSeatsPerRow;
    this.rightSeatsPerRow = rightSeatsPerRow;
  }

  public int capacity() {
    return capacity;
  }

  public int rows() {
    return rows;
  }

  public int leftSeatsPerRow() {
    return leftSeatsPerRow;
  }

  public int rightSeatsPerRow() {
    return rightSeatsPerRow;
  }

  public static VehicleSeatLayout fromCapacity(int capacity) {
    for (VehicleSeatLayout layout : values()) {
      if (layout.capacity == capacity) {
        return layout;
      }
    }
    return null;
  }
}
