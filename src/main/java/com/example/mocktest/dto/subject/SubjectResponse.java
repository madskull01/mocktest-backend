package com.example.mocktest.dto.subject;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectResponse {

    private Long id;

    private String name;

    private String description;

    private boolean active;

    private Long sectionId;

    private String sectionName;
}
