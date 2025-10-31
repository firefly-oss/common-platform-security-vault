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

import com.firefly.common.security.vault.core.services.CredentialTypeService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialTypeDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST controller for managing credential types
 */
@RestController
@RequestMapping("/api/v1/credential-types")
@RequiredArgsConstructor
@Tag(name = "Credential Types", description = "API for managing credential types")
public class CredentialTypeController {

    private final CredentialTypeService credentialTypeService;

    @GetMapping("/{id}")
    @Operation(
            operationId = "getCredentialTypeById",
            summary = "Get a credential type by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credential type found"),
                    @ApiResponse(responseCode = "404", description = "Credential type not found")
            }
    )
    public ResponseEntity<Mono<CredentialTypeDTO>> getById(
            @Parameter(description = "ID of the credential type to retrieve", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(credentialTypeService.getById(id));
    }

    @PostMapping("/filter")
    @Operation(
            operationId = "filterCredentialTypes",
            summary = "Filter credential types",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credential types filtered successfully")
            }
    )
    public ResponseEntity<Mono<PaginationResponse<CredentialTypeDTO>>> filter(
            @Parameter(description = "Filter criteria", required = true)
            @Valid @RequestBody FilterRequest<CredentialTypeDTO> filterRequest) {
        return ResponseEntity.ok(credentialTypeService.filter(filterRequest));
    }

    @PostMapping
    @Operation(
            operationId = "createCredentialType",
            summary = "Create a new credential type",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Credential type created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    public ResponseEntity<Mono<CredentialTypeDTO>> create(
            @Parameter(description = "Credential type to create", required = true)
            @Valid @RequestBody CredentialTypeDTO credentialTypeDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(credentialTypeService.create(credentialTypeDTO));
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "updateCredentialType",
            summary = "Update an existing credential type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credential type updated"),
                    @ApiResponse(responseCode = "404", description = "Credential type not found")
            }
    )
    public ResponseEntity<Mono<CredentialTypeDTO>> update(
            @Parameter(description = "ID of the credential type to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Credential type to update", required = true)
            @Valid @RequestBody CredentialTypeDTO credentialTypeDTO) {
        return ResponseEntity.ok(credentialTypeService.update(id, credentialTypeDTO));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteCredentialType",
            summary = "Delete a credential type",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Credential type deleted"),
                    @ApiResponse(responseCode = "404", description = "Credential type not found")
            }
    )
    public Mono<ResponseEntity<Void>> delete(
            @Parameter(description = "ID of the credential type to delete", required = true)
            @PathVariable UUID id) {
        return credentialTypeService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}

