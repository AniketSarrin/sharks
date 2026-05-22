package com.sharks.ticketing.service;

import com.sharks.ticketing.dto.PurchaseTicketRequest;
import com.sharks.ticketing.dto.TicketReceiptResponse;
import com.sharks.ticketing.messaging.EventCreatedMessage;
import com.sharks.ticketing.security.AuthPrincipal;

import java.util.List;

public interface TicketService {

	TicketReceiptResponse purchase(PurchaseTicketRequest request, AuthPrincipal principal);

	List<TicketReceiptResponse> getMyTickets(AuthPrincipal principal);

	void provisionFromEventCreated(EventCreatedMessage message);
}
