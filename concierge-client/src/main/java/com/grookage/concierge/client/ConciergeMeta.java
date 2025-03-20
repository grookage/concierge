package com.grookage.concierge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConciergeMeta {

    @Builder.Default
    private Set<String> namespaces = Set.of();
    @Builder.Default
    private Set<String> orgs = Set.of();
    @Builder.Default
    private Set<String> tenants = Set.of();
}
