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

import com.firefly.common.security.vault.core.services.CredentialService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST controller for managing credentials
 */
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
@Tag(name = "Credentials", description = "API for managing secure credentials")
public class CredentialController {

    private final CredentialService credentialService;

    /**
     * GET /api/v1/credentials/:id : Get a credential by ID
     *
     * @param id the ID of the credential to retrieve
     * @return the ResponseEntity with status 200 (OK) and the credential in the body, or status 404 (Not Found)
     */
    @GetMapping("/{id}")
    @Operation(
            operationId = "getCredentialById",
            summary = "Get a credential by ID",
            description = "Returns a credential based on the ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = CredentialDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Credential not found")
            }
    )
    public ResponseEntity<Mono<CredentialDTO>> getById(
            @Parameter(description = "ID of the credential to retrieve", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(credentialService.getById(id));
    }

    /**
     * POST /api/v1/credentials/filter : Filter credentials
     *
     * @param filterRequest the filter criteria
     * @return the ResponseEntity with status 200 (OK) and the list of credentials in the body
     */
    @PostMapping("/filter")
    @Operation(
            operationId = "filterCredentials",
            summary = "Filter credentials",
            description = "Returns a filtered list of credentials based on criteria",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation")
            }
    )
    public ResponseEntity<Mono<PaginationResponse<CredentialDTO>>> filter(
            @ParameterObject @ModelAttribute FilterRequest<CredentialDTO> filterRequest) {
        return ResponseEntity.ok(credentialService.filter(filterRequest));
    }

    /**
     * POST /api/v1/credentials : Create a new credential
     *
     * @param credentialDTO the credential to create
     * @return the ResponseEntity with status 201 (Created) and the new credential in the body
     */
    @PostMapping
    @Operation(
            operationId = "createCredential",
            summary = "Create a new credential",
            description = "Creates a new credential and returns it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Credential created", content = @Content(schema = @Schema(implementation = CredentialDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    public ResponseEntity<Mono<CredentialDTO>> create(
            @Parameter(description = "Credential to create", required = true)
            @Valid @RequestBody CredentialDTO credentialDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(credentialService.create(credentialDTO));
    }

    /**
     * PUT /api/v1/credentials/:id : Update an existing credential
     *
     * @param id the ID of the credential to update
     * @param credentialDTO the credential to update
     * @return the ResponseEntity with status 200 (OK) and the updated credential in the body, or status 404 (Not Found)
     */
    @PutMapping("/{id}")
    @Operation(
            operationId = "updateCredential",
            summary = "Update an existing credential",
            description = "Updates an existing credential and returns it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credential updated", content = @Content(schema = @Schema(implementation = CredentialDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Credential not found")
            }
    )
    public ResponseEntity<Mono<CredentialDTO>> update(
            @Parameter(description = "ID of the credential to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Credential to update", required = true)
            @Valid @RequestBody CredentialDTO credentialDTO) {
        return ResponseEntity.ok(credentialService.update(id, credentialDTO));
    }

    /**
     * DELETE /api/v1/credentials/:id : Delete a credential
     *
     * @param id the ID of the credential to delete
     * @return the ResponseEntity with status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteCredential",
            summary = "Delete a credential",
            description = "Deletes a credential",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Credential deleted"),
                    @ApiResponse(responseCode = "404", description = "Credential not found")
            }
    )
    public Mono<ResponseEntity<Void>> delete(
            @Parameter(description = "ID of the credential to delete", required = true)
            @PathVariable UUID id) {
        return credentialService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
