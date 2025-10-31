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

import com.firefly.common.security.vault.core.services.EncryptionKeyService;
import com.firefly.common.security.vault.interfaces.dtos.EncryptionKeyDTO;
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
@RequestMapping("/api/v1/encryption-keys")
@RequiredArgsConstructor
@Tag(name = "Encryption Keys", description = "API for managing encryption keys metadata")
public class EncryptionKeyController {

    private final EncryptionKeyService encryptionKeyService;

    @GetMapping("/{id}")
    @Operation(operationId = "getEncryptionKeyById", summary = "Get an encryption key by ID")
    public ResponseEntity<Mono<EncryptionKeyDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(encryptionKeyService.getById(id));
    }

    @PostMapping("/filter")
    @Operation(operationId = "filterEncryptionKeys", summary = "Filter encryption keys")
    public ResponseEntity<Mono<PaginationResponse<EncryptionKeyDTO>>> filter(
            @Valid @RequestBody FilterRequest<EncryptionKeyDTO> filterRequest) {
        return ResponseEntity.ok(encryptionKeyService.filter(filterRequest));
    }

    @PostMapping
    @Operation(operationId = "createEncryptionKey", summary = "Create a new encryption key")
    public ResponseEntity<Mono<EncryptionKeyDTO>> create(@Valid @RequestBody EncryptionKeyDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(encryptionKeyService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateEncryptionKey", summary = "Update an existing encryption key")
    public ResponseEntity<Mono<EncryptionKeyDTO>> update(@PathVariable UUID id, @Valid @RequestBody EncryptionKeyDTO dto) {
        return ResponseEntity.ok(encryptionKeyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteEncryptionKey", summary = "Delete an encryption key")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return encryptionKeyService.delete(id).then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}

