package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
public class Registration {
    private int id;
    private String memberId;
    private String memberName;
    private String lectureCode;
    private String lectureName;
    private String professor;
    private int credit;
    private int lectureId;
}
