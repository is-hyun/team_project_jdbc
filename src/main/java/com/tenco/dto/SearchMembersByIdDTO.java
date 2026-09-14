package com.tenco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SearchMembersByIdDTO {
    private int id;
    private String name;
    private String phone;
    private String major;
    private int grade;
}
