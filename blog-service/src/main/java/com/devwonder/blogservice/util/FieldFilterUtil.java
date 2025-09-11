package com.devwonder.blogservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FieldFilterUtil {
    
    private final ObjectMapper objectMapper;
    
    public <T> T applyFieldFiltering(T object, String fields) {
        if (fields == null || fields.trim().isEmpty()) {
            return object;
        }
        
        try {
            // Convert object to JsonNode
            JsonNode jsonNode = objectMapper.valueToTree(object);
            
            // Parse requested fields
            Set<String> requestedFields = new HashSet<>(Arrays.asList(fields.split(",")));
            requestedFields.replaceAll(String::trim);
            
            // Create filtered object
            ObjectNode filteredNode = objectMapper.createObjectNode();
            
            requestedFields.forEach(field -> {
                if (jsonNode.has(field)) {
                    filteredNode.set(field, jsonNode.get(field));
                }
            });
            
            // Convert back to original type
            @SuppressWarnings("unchecked")
            T result = (T) objectMapper.treeToValue(filteredNode, object.getClass());
            
            return result;
            
        } catch (Exception e) {
            log.warn("Failed to apply field filtering for fields: {}, returning original object", fields, e);
            return object;
        }
    }
}