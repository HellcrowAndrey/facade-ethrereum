package com.github.facade.ethrereum.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Objects;

public class EthereumChainInfo {

    private final EthereumBlock block;

    private final BigInteger gasPrice;

    @JsonCreator
    public EthereumChainInfo(
            @JsonProperty(value = "block") EthereumBlock block,
            @JsonProperty(value = "gasPrice") BigInteger gasPrice) {
        this.block = block;
        this.gasPrice = gasPrice;
    }

    public EthereumBlock getBlock() {
        return block;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthereumChainInfo that = (EthereumChainInfo) o;
        return Objects.equals(block, that.block) &&
                Objects.equals(gasPrice, that.gasPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, gasPrice);
    }

    @Override
    public String toString() {
        return "{block: " +
                this.block +
                ", gasPrice: " +
                this.gasPrice + "}";
    }

}
