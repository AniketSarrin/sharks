package com.sharks.ticketing.controller;

import com.sharks.ticketing.dto.PurchaseTicketRequest;
import com.sharks.ticketing.dto.TicketReceiptResponse;
import com.sharks.ticketing.security.AuthPrincipal;
import com.sharks.ticketing.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	/**
	 * POST /api/tickets/purchase - Purchase tickets.
	 */
	@PostMapping("/tickets/purchase")
	@ResponseStatus(HttpStatus.CREATED)
	public TicketReceiptResponse purchaseTickets(
			@Valid @RequestBody PurchaseTicketRequest request,
			@AuthenticationPrincipal AuthPrincipal principal) {
		return ticketService.purchase(request, principal);
	}

	/**
	 * GET /api/tickets/my-tickets - Get user's tickets.
	 */
	@GetMapping("/tickets/my-tickets")
	public List<TicketReceiptResponse> getMyTickets(@AuthenticationPrincipal AuthPrincipal principal) {
		return ticketService.getMyTickets(principal);
	}
}
