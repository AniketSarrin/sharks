package com.sharks.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event.rabbitmq")
public class EventRabbitProperties {

	private String eventExchange = "sharks.event";

	private String eventCancelledRoutingKey = "event.cancelled";

	private String eventCreatedRoutingKey = "event.created";

	public String getEventExchange() {
		return eventExchange;
	}

	public void setEventExchange(String eventExchange) {
		this.eventExchange = eventExchange;
	}

	public String getEventCancelledRoutingKey() {
		return eventCancelledRoutingKey;
	}

	public void setEventCancelledRoutingKey(String eventCancelledRoutingKey) {
		this.eventCancelledRoutingKey = eventCancelledRoutingKey;
	}

	public String getEventCreatedRoutingKey() {
		return eventCreatedRoutingKey;
	}

	public void setEventCreatedRoutingKey(String eventCreatedRoutingKey) {
		this.eventCreatedRoutingKey = eventCreatedRoutingKey;
	}
}
