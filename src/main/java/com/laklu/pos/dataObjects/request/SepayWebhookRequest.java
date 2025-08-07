package com.laklu.pos.dataObjects.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class SepayWebhookRequest {
    @JsonProperty("gateway")
    private String gateway;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("subAccount")
    private String subAccount;

    @JsonProperty("code")
    private String code;

    @JsonProperty("content")
    private String content;

    @JsonProperty("transferType")
    private String transferType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("transferAmount")
    private BigDecimal transferAmount;

    @JsonProperty("referenceCode")
    private String referenceCode;

    @JsonProperty("accumulated")
    private int accumulated;

    @JsonProperty("id")
    private int id;
}
