package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
@ToString
public class Registration {
    private int id;
    private int memberId;
    private int lectureId;
}
