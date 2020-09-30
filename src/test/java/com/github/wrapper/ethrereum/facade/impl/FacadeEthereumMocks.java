package com.github.wrapper.ethrereum.facade.impl;

import com.github.wrapper.ethrereum.model.KeyPair;

import java.math.BigInteger;

public class FacadeEthereumMocks {

    public static final BigInteger PRIVATE_KEY = new BigInteger("100895495268790251376912975136787819366858501190262293512126098766069993773048");

    public static final BigInteger PUBLIC_KEY = new BigInteger("9401955856600962629311101705393210030457726952314846327504770302893759938843867397057501063903299130607873524813504696500333270490896388350007560789853196");

    public static final String ADDRESS = "0x5f955d59be17d6787600810d3eff3584259734cc";

    public static KeyPair keyPair() {
        return new KeyPair(PRIVATE_KEY, PUBLIC_KEY, ADDRESS);
    }

}
