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

import com.firefly.common.security.vault.core.services.CredentialStatusService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialStatusDTO;
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

@RestController
@RequestMapping("/api/v1/credential-statuses")
@RequiredArgsConstructor
@Tag(name = "Credential Statuses", description = "API for managing credential statuses")
public class CredentialStatusController {

    private final CredentialStatusService credentialStatusService;

    @GetMapping("/{id}")
    @Operation(operationId = "getCredentialStatusById", summary = "Get a credential status by ID")
    public ResponseEntity<Mono<CredentialStatusDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(credentialStatusService.getById(id));
    }

    @PostMapping("/filter")
    @Operation(operationId = "filterCredentialStatuses", summary = "Filter credential statuses")
    public ResponseEntity<Mono<PaginationResponse<CredentialStatusDTO>>> filter(
            @Valid @RequestBody FilterRequest<CredentialStatusDTO> filterRequest) {
        return ResponseEntity.ok(credentialStatusService.filter(filterRequest));
    }

    @PostMapping
    @Operation(operationId = "createCredentialStatus", summary = "Create a new credential status")
    public ResponseEntity<Mono<CredentialStatusDTO>> create(@Valid @RequestBody CredentialStatusDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(credentialStatusService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateCredentialStatus", summary = "Update an existing credential status")
    public ResponseEntity<Mono<CredentialStatusDTO>> update(@PathVariable UUID id, @Valid @RequestBody CredentialStatusDTO dto) {
        return ResponseEntity.ok(credentialStatusService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteCredentialStatus", summary = "Delete a credential status")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return credentialStatusService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}

