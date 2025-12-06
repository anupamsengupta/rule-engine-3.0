package com.ruleengine.infrastructure.model;

import java.math.BigDecimal;
import java.util.Objects;

public record User(String loginCode, String userId, BigDecimal limit, UserStatus status, String mailId) {
}
