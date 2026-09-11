package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
@ToString
public class Lectures {
    int id;
    String lectureCode;
    String lectureName;
    String professor;
    int credit;
    int capacity;
    boolean available;
}
