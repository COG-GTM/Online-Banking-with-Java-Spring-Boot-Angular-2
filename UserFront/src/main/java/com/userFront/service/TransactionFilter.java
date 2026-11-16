package com.userFront.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

public class TransactionFilter {

	public static final String TYPE_DEPOSIT = "deposit";
	public static final String TYPE_WITHDRAW = "withdraw";
	public static final String TYPE_TRANSFER = "transfer";

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private Date from;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private Date to;

	private String type;
	private Double minAmount;
	private Double maxAmount;
	private String q;

	public boolean isActive() {
		return from != null || to != null || StringUtils.hasText(type) || minAmount != null || maxAmount != null
				|| StringUtils.hasText(q);
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(Double minAmount) {
		this.minAmount = minAmount;
	}

	public Double getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(Double maxAmount) {
		this.maxAmount = maxAmount;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}
}
