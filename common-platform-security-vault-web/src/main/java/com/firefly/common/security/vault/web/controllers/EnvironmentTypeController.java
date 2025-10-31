/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.common.security.vault.web.controllers;

import com.firefly.common.security.vault.core.services.EnvironmentTypeService;
import com.firefly.common.security.vault.interfaces.dtos.EnvironmentTypeDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/environment-types")
@RequiredArgsConstructor
@Tag(name = "Environment Types", description = "API for managing environment types")
public class EnvironmentTypeController {

    private final EnvironmentTypeService environmentTypeService;

    @GetMapping("/{id}")
    @Operation(operationId = "getEnvironmentTypeById", summary = "Get an environment type by ID")
    public ResponseEntity<Mono<EnvironmentTypeDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(environmentTypeService.getById(id));
    }

    @PostMapping("/filter")
    @Operation(operationId = "filterEnvironmentTypes", summary = "Filter environment types")
    public ResponseEntity<Mono<PaginationResponse<EnvironmentTypeDTO>>> filter(
            @Valid @RequestBody FilterRequest<EnvironmentTypeDTO> filterRequest) {
        return ResponseEntity.ok(environmentTypeService.filter(filterRequest));
    }

    @PostMapping
    @Operation(operationId = "createEnvironmentType", summary = "Create a new environment type")
    public ResponseEntity<Mono<EnvironmentTypeDTO>> create(@Valid @RequestBody EnvironmentTypeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(environmentTypeService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateEnvironmentType", summary = "Update an existing environment type")
    public ResponseEntity<Mono<EnvironmentTypeDTO>> update(@PathVariable UUID id, @Valid @RequestBody EnvironmentTypeDTO dto) {
        return ResponseEntity.ok(environmentTypeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteEnvironmentType", summary = "Delete an environment type")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return environmentTypeService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}

