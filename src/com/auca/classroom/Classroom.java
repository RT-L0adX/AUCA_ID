package com.auca.classroom;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

public class Classroom implements Identifiable, Registrable {
    private String classroomId;
    private String buildingName;
    private String roomNumber;
    private int capacity;
    private int floorNumber;
    private boolean available;

    public Classroom(String classroomId, String buildingName, String roomNumber,
                     int capacity, int floorNumber, boolean available) {
        this.classroomId = Registrable.clean(classroomId);
        this.buildingName = Registrable.clean(buildingName);
        this.roomNumber = Registrable.clean(roomNumber);
        this.capacity = capacity;
        this.floorNumber = floorNumber;
        this.available = available;
    }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = Registrable.clean(classroomId); }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = Registrable.clean(buildingName); }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = Registrable.clean(roomNumber); }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String getId() {
        return classroomId;
    }

    @Override
    public String getDisplayName() {
        return buildingName + " " + roomNumber;
    }

    @Override
    public boolean isActive() {
        return available;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Classroom ID", classroomId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireLength("Building name", buildingName, 2, 60);
        Registrable.requireLength("Room number", roomNumber, 1, 20);
        Registrable.requireMin("Capacity", capacity, 1);
        Registrable.requireMin("Floor number", floorNumber, 0);
    }

    @Override
    public void printDetails() {
        System.out.println("Classroom ID  : " + classroomId);
        System.out.println("Building      : " + buildingName);
        System.out.println("Room number   : " + roomNumber);
        System.out.println("Capacity      : " + capacity);
        System.out.println("Floor         : " + floorNumber);
        System.out.println("Available     : " + (available ? "YES" : "NO"));
    }

    @Override
    public String getRegistrationSummary() {
        return "[" + classroomId + "] " + getDisplayName() + " (capacity " + capacity + ")";
    }

    @Override
    public String toString() {
        return "Classroom [classroomId=" + classroomId + ", buildingName=" + buildingName
                + ", roomNumber=" + roomNumber + ", capacity=" + capacity
                + ", floorNumber=" + floorNumber + ", available=" + available + "]";
    }
}