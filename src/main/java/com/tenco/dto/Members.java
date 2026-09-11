package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
@ToString
public class Members {
    int id;
    String memberId;
    String name;
    String phone;
    String major;
    int grade;
    boolean admin;
}
