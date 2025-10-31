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

import com.firefly.common.security.vault.core.services.CredentialVersionService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialVersionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credential-versions")
@RequiredArgsConstructor
@Tag(name = "Credential Versions", description = "API for managing credential version history")
public class CredentialVersionController {

    private final CredentialVersionService credentialVersionService;

    @GetMapping("/{id}")
    @Operation(operationId = "getCredentialVersionById", summary = "Get a credential version by ID")
    public ResponseEntity<Mono<CredentialVersionDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(credentialVersionService.getById(id));
    }

    @GetMapping("/credential/{credentialId}")
    @Operation(operationId = "getVersionsByCredentialId", summary = "Get all versions for a credential")
    public ResponseEntity<Flux<CredentialVersionDTO>> getVersionsByCredentialId(
            @Parameter(description = "Credential ID", required = true)
            @PathVariable UUID credentialId) {
        return ResponseEntity.ok(credentialVersionService.getVersionsByCredentialId(credentialId));
    }

    @GetMapping("/credential/{credentialId}/current")
    @Operation(operationId = "getCurrentVersion", summary = "Get current version for a credential")
    public ResponseEntity<Mono<CredentialVersionDTO>> getCurrentVersion(
            @Parameter(description = "Credential ID", required = true)
            @PathVariable UUID credentialId) {
        return ResponseEntity.ok(credentialVersionService.getCurrentVersion(credentialId));
    }

    @PostMapping
    @Operation(operationId = "createCredentialVersion", summary = "Create a new credential version")
    public ResponseEntity<Mono<CredentialVersionDTO>> create(@Valid @RequestBody CredentialVersionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(credentialVersionService.create(dto));
    }
}

