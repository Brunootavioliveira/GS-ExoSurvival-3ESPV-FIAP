package com.exosurvival.controller;

import com.exosurvival.dto.request.GameSessionRequest;
import com.exosurvival.dto.response.ApiResponse;
import com.exosurvival.dto.response.GameSessionResponse;
import com.exosurvival.service.GameSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class GameSessionController {

    private final GameSessionService gameSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<GameSessionResponse>> save(
            @Valid @RequestBody GameSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        GameSessionResponse response = gameSessionService.save(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Session saved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GameSessionResponse>>> list(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<GameSessionResponse> sessions = gameSessionService.listByUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @GetMapping("/best")
    public ResponseEntity<ApiResponse<GameSessionResponse>> getBest(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        GameSessionResponse best = gameSessionService.getBestSession(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(best));
    }
}
