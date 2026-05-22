package com.sharks.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EventIdsRequest(
		@NotNull @Size(max = 500) List<@Positive Long> ids) {
}
