package com.grookage.concierge.core.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CacheConfig {

    private boolean enabled;
    @Builder.Default
    private Set<String> configTypes = Set.of();
    @Builder.Default
    private int refreshCacheSeconds = 10;

    public boolean cachedType(final String configType) {
        return configTypes != null && configTypes.stream()
                .anyMatch(each -> each.equalsIgnoreCase(configType));
    }
}
