package com.github.wrapper.ethrereum.model;

import java.math.BigInteger;
import java.util.Objects;

public class KeyPair {

    private final BigInteger privateKey;

    private final BigInteger publicKey;

    private final String address;

    public KeyPair(BigInteger privateKey, BigInteger publicKey, String address) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyPair keyPair = (KeyPair) o;
        return Objects.equals(privateKey, keyPair.privateKey) &&
                Objects.equals(publicKey, keyPair.publicKey) &&
                Objects.equals(address, keyPair.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateKey, publicKey, address);
    }

    @Override
    public String toString() {
        return "KeyPair{" +
                "privateKey=" + privateKey +
                ", publicKey=" + publicKey +
                ", address='" + address + '\'' +
                '}';
    }
}
