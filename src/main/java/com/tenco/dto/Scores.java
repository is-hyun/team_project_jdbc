package com.tenco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
public class Score {
    private int id;
    private String memberId;
    private String name;
    private String lectureId;
    private String lectureName;
    private int score;
}
