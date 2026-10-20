package com.example.mocktest.dto.section;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionResponse {

    private Long id;

    private String name;

    private String description;

    private boolean active;
}
