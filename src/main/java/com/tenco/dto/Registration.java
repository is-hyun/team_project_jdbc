package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
public class Registration {
    private int id;
    private int memberId;
    private String memberName;
    private int lectureId;

}
