package com.tenco.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data //getter setter 모두 포함
@Builder
@ToString
public class Members {
    private int id;
    private String memberId;
    private String password;
    private String name;
    private String phone;
    private String major;
    private int grade;
    private boolean admin;

    private Integer score;
}
