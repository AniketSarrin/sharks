package com.sharks.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(UserRabbitProperties.class)
public class RabbitMqConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		return mapper;
	}

	@Bean
	public TopicExchange userEventsExchange(UserRabbitProperties properties) {
		return new TopicExchange(properties.getUserExchange(), true, false);
	}

	@Bean
	public Queue userCreatedQueue(UserRabbitProperties properties) {
		return new Queue(properties.getUserQueue(), true);
	}

	@Bean
	public Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange userEventsExchange,
			UserRabbitProperties properties) {
		return BindingBuilder.bind(userCreatedQueue).to(userEventsExchange).with(properties.getUserRoutingKey());
	}

	@Bean
	public TopicExchange authEventsExchange() {
		return new TopicExchange("sharks.auth", true, false);
	}

	@Bean
	public Queue userProvisionedQueue() {
		return new Queue("user.user.provisioned", true);
	}

	@Bean
	public Binding userProvisionedBinding(Queue userProvisionedQueue, TopicExchange authEventsExchange) {
		return BindingBuilder.bind(userProvisionedQueue).to(authEventsExchange).with("user.provisioned");
	}

	@Bean
	public Queue userRoleChangedQueue() {
		return new Queue("user.role.changed", true);
	}

	@Bean
	public Binding userRoleChangedBinding(Queue userRoleChangedQueue, TopicExchange authEventsExchange) {
		return BindingBuilder.bind(userRoleChangedQueue).to(authEventsExchange).with("role.changed");
	}

	@Bean
	@Primary
	public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
			MessageConverter jackson2JsonMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jackson2JsonMessageConverter);
		return factory;
	}
}
