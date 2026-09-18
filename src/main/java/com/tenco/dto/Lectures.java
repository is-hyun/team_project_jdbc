package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
@ToString
public class Lectures {
    private int id;
    private String lectureCode;
    private String lectureName;
    private String professor;
    private int credit;
    private int capacity;
    // private boolean available;

    private int enrolled;

    public boolean isAvailable() {
        return this.capacity > this.enrolled;
    }
}
