package com.grookage.concierge.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageWindow {
    @Builder.Default
    @Min(0)
    private int page = 0;
    @Builder.Default
    @Max(10000)
    private int pageSize = 100;

    public int offset() {
        return page * pageSize;
    }
}
