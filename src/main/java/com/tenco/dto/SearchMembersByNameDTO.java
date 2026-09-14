package com.tenco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SearchMembersByNameDTO {
    private String memberId;
    private String name;
    private String phone;
    private String major;
    private int grade;
    private Integer score;
}
