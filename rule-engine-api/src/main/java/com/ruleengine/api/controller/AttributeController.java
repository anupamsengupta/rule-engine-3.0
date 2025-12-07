package com.ruleengine.api.controller;

import com.ruleengine.api.dto.AttributeDto;
import com.ruleengine.api.dto.CreateAttributeRequest;
import com.ruleengine.api.dto.UpdateAttributeRequest;
import com.ruleengine.application.service.AttributeService;
import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Attribute CRUD operations.
 *
 * Module: rule-engine-api
 * Layer: API
 */
@RestController
@RequestMapping("/api/attributes")
public class AttributeController {
    private final AttributeService attributeService;

    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @PostMapping
    public ResponseEntity<AttributeDto> createAttribute(@RequestBody CreateAttributeRequest request) {
        try {
            Attribute attribute = new Attribute(
                    request.code(),
                    request.path() != null ? request.path() : request.code(),
                    AttributeType.valueOf(request.type()),
                    Optional.ofNullable(request.description()),
                    Optional.ofNullable(request.constraints())
            );
            Attribute created = attributeService.createAttribute(attribute);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<AttributeDto> getAttribute(@PathVariable String code) {
        return attributeService.getAttributeByCode(code)
                .map(attr -> ResponseEntity.ok(toDto(attr)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AttributeDto>> getAllAttributes() {
        List<AttributeDto> attributes = attributeService.getAllAttributes().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attributes);
    }

    @PutMapping("/{code}")
    public ResponseEntity<AttributeDto> updateAttribute(
            @PathVariable String code,
            @RequestBody UpdateAttributeRequest request
    ) {
        try {
            // Verify attribute exists
            if (!attributeService.attributeExists(code)) {
                return ResponseEntity.notFound().build();
            }
            
            Attribute updated = new Attribute(
                    code,
                    request.path() != null ? request.path() : code,
                    AttributeType.valueOf(request.type()),
                    Optional.ofNullable(request.description()),
                    Optional.ofNullable(request.constraints())
            );
            Attribute saved = attributeService.updateAttribute(updated);
            return ResponseEntity.ok(toDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable String code) {
        try {
            attributeService.deleteAttribute(code);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private AttributeDto toDto(Attribute attribute) {
        return new AttributeDto(
                attribute.code(),
                attribute.path(),
                attribute.type().name(),
                attribute.description().orElse(null),
                attribute.constraints().orElse(null)
        );
    }
}

