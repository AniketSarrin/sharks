package com.sharks.ticketing.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.sharks.ticketing.service.TicketService;

@Component
public class EventCreatedListener {

	private static final Logger log = LoggerFactory.getLogger(EventCreatedListener.class);

	private final TicketService ticketService;

	public EventCreatedListener(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@RabbitListener(queues = "${ticketing.rabbitmq.event-created-queue:user.event.created}")
	public void onEventCreated(EventCreatedMessage message) {
		if (message == null) {
			log.warn("Received null event.created payload");
			return;
		}
		log.info("Received event.created: eventId={}", message.eventId());
		ticketService.provisionFromEventCreated(message);
	}
}
