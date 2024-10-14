package com.good.ivrstand.extern.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionUpdateDTO {

    private String description;

    private boolean enableAudio;
}