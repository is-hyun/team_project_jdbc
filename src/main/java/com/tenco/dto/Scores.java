package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
@ToString
public class Scores {
    int id;
    int memberId;
    int lectureId;
    int score;
}
