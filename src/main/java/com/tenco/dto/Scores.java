package com.tenco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
public class Scores {
    int id;
    String memberId;
    String lectureId;
    int score;
}
