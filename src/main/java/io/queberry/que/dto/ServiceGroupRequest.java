package io.queberry.que.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceGroupRequest {
    private Set<String> services;
    private String name;
    private String displayName;
    private Map<String,String> names;
}
