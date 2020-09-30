package com.github.wrapper.ethrereum.model;

import java.math.BigInteger;
import java.util.Objects;

public final class Information {

    private final BigInteger fee;

    private final BigInteger gasPrice;

    public Information(BigInteger fee, BigInteger gasPrice) {
        this.fee = fee;
        this.gasPrice = gasPrice;
    }

    public BigInteger getFee() {
        return fee;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Information that = (Information) o;
        return Objects.equals(fee, that.fee) &&
                Objects.equals(gasPrice, that.gasPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fee, gasPrice);
    }

    @Override
    public String toString() {
        return "Information{" +
                "fee=" + fee +
                ", gasPrice=" + gasPrice +
                '}';
    }
}
