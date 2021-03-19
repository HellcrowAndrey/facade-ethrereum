package com.github.facade.ethrereum.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class EthereumBlock {

    private final String hash;

    private final BigInteger number;

    private final BigInteger nonce;

    private final BigInteger size;

    private final BigInteger gasLimit;

    private final BigInteger gasUsed;

    private final List<TransactionData> transactions;

    @JsonCreator
    public EthereumBlock(
            @JsonProperty(value = "hash") String hash,
            @JsonProperty(value = "number") BigInteger number,
            @JsonProperty(value = "nonce") BigInteger nonce,
            @JsonProperty(value = "size")BigInteger size,
            @JsonProperty(value = "gasLimit") BigInteger gasLimit,
            @JsonProperty(value = "gasUsed")BigInteger gasUsed,
            @JsonProperty(value = "transactions") List<TransactionData> transactions) {
        this.hash = hash;
        this.number = number;
        this.nonce = nonce;
        this.size = size;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.transactions = transactions;
    }

    public String getHash() {
        return hash;
    }

    public BigInteger getNumber() {
        return number;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getSize() {
        return size;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public List<TransactionData> getTransactions() {
        return transactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthereumBlock that = (EthereumBlock) o;
        return Objects.equals(hash, that.hash) &&
                Objects.equals(number, that.number) &&
                Objects.equals(nonce, that.nonce) &&
                Objects.equals(size, that.size) &&
                Objects.equals(gasLimit, that.gasLimit) &&
                Objects.equals(gasUsed, that.gasUsed) &&
                Objects.equals(transactions, that.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, number, nonce, size, gasLimit, gasUsed, transactions);
    }

    @Override
    public String toString() {
        return "{" +
                "number: " + number +
                ", hash: " + hash +
                ", transactions: " + transactions.size() +
                '}';
    }

}
