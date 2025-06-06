package com.grookage.concierge.core.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CacheConfig {

    private boolean enabled;
    @Builder.Default
    private int refreshCacheSeconds = 10;
}
