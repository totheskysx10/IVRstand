package com.good.ivrstand.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TitleRequest {
    private String text;
    private long id;
}
