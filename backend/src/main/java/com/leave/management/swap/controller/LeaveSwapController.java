package com.leave.management.swap.controller;

import com.leave.management.employee.model.Employee;
import com.leave.management.security.UserPrincipal;
import com.leave.management.swap.dto.CreateSwapRequestDto;
import com.leave.management.swap.dto.EligibleSwapDto;
import com.leave.management.swap.dto.SwapResponseDto;
import com.leave.management.swap.service.LeaveSwapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swaps")
public class LeaveSwapController {

    private final LeaveSwapService swapService;

    public LeaveSwapController(LeaveSwapService swapService) {
        this.swapService = swapService;
    }

    @GetMapping("/eligible/{leaveRequestId}")
    public ResponseEntity<List<EligibleSwapDto>> getEligibleSwaps(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long leaveRequestId) {
        List<EligibleSwapDto> swaps = swapService.getEligibleSwaps(userPrincipal.getEmployee(), leaveRequestId);
        return ResponseEntity.ok(swaps);
    }

    @PostMapping("/propose")
    public ResponseEntity<SwapResponseDto> proposeSwap(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CreateSwapRequestDto dto) {
        SwapResponseDto response = swapService.proposeSwap(userPrincipal.getEmployee(), dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{swapId}/accept")
    public ResponseEntity<SwapResponseDto> acceptSwap(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long swapId) {
        SwapResponseDto response = swapService.acceptSwap(swapId, userPrincipal.getEmployee());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{swapId}/decline")
    public ResponseEntity<SwapResponseDto> declineSwap(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long swapId) {
        SwapResponseDto response = swapService.declineSwap(swapId, userPrincipal.getEmployee());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{swapId}/approve")
    public ResponseEntity<SwapResponseDto> approveSwap(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long swapId) {
        SwapResponseDto response = swapService.approveSwap(swapId, userPrincipal.getEmployee());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-manager")
    public ResponseEntity<List<SwapResponseDto>> getPendingSwapsForManager(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<SwapResponseDto> swaps = swapService.getPendingSwapsForManager(userPrincipal.getEmployee().getId());
        return ResponseEntity.ok(swaps);
    }

    @GetMapping("/my-swaps")
    public ResponseEntity<List<SwapResponseDto>> getMySwaps(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<SwapResponseDto> swaps = swapService.getSwapsForEmployee(userPrincipal.getEmployee().getId());
        return ResponseEntity.ok(swaps);
    }
}
