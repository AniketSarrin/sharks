package com.sharks.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user.rabbitmq")
public class UserRabbitProperties {

	private String userExchange = "sharks.user";

	private String userQueue = "user.user.created";

	private String userRoutingKey = "user.created";

	public String getUserExchange() {
		return userExchange;
	}

	public void setUserExchange(String userExchange) {
		this.userExchange = userExchange;
	}

	public String getUserQueue() {
		return userQueue;
	}

	public void setUserQueue(String userQueue) {
		this.userQueue = userQueue;
	}

	public String getUserRoutingKey() {
		return userRoutingKey;
	}

	public void setUserRoutingKey(String userRoutingKey) {
		this.userRoutingKey = userRoutingKey;
	}
}
