package com.exosurvival.controller;

import com.exosurvival.dto.request.PlanetRequest;
import com.exosurvival.dto.response.ApiResponse;
import com.exosurvival.dto.response.PlanetResponse;
import com.exosurvival.service.PlanetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/planets")
@RequiredArgsConstructor
public class PlanetController {

    private final PlanetService planetService;

    @PostMapping
    public ResponseEntity<ApiResponse<PlanetResponse>> create(
            @Valid @RequestBody PlanetRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PlanetResponse response = planetService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Planet created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanetResponse>>> list(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<PlanetResponse> planets = planetService.listByUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(planets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanetResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PlanetResponse response = planetService.getById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        planetService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Planet deleted", null));
    }
}
