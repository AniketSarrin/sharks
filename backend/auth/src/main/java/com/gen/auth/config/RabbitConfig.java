package com.gen.auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

	private static final String USER_EXCHANGE = "sharks.user";

	@Bean
	public TopicExchange userExchange() {
		return new TopicExchange(USER_EXCHANGE, true, false);
	}

	@Bean
	public Queue authUserCreatedQueue() {
		return new Queue("auth.user.created", true);
	}

	@Bean
	public Queue authUserDeletedQueue() {
		return new Queue("auth.user.deleted", true);
	}

	@Bean
	public Binding authUserCreatedBinding(Queue authUserCreatedQueue, TopicExchange userExchange) {
		return BindingBuilder.bind(authUserCreatedQueue).to(userExchange).with("user.created");
	}

	@Bean
	public Binding authUserDeletedBinding(Queue authUserDeletedQueue, TopicExchange userExchange) {
		return BindingBuilder.bind(authUserDeletedQueue).to(userExchange).with("user.deleted");
	}

	@Bean
	public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
		ConnectionFactory connectionFactory,
		MessageConverter jacksonJsonMessageConverter
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jacksonJsonMessageConverter);
		return factory;
	}
}
