package com.kruzetech.vocab.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meaning {

    private int order;

    private String pos;

    @JsonProperty("meaning_vi")
    private String meaningVi;

    @JsonProperty("definition_en")
    private String definitionEn;

    @JsonProperty("example_en")
    private String exampleEn;

    @JsonProperty("example_vi")
    private String exampleVi;
}
