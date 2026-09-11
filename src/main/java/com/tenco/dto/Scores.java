package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
@ToString
public class Scores {
    private int id;
    private String memberId;
    private String name;
    private String lectureId;
    private String lectureName;
    private int score;
}
