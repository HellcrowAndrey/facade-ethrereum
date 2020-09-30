package com.github.wrapper.ethrereum.facade;

import com.github.wrapper.ethrereum.model.KeyPair;
import com.github.wrapper.ethrereum.model.TransactionData;

import java.math.BigInteger;

public interface IFacadeTokenERC20 {

    TransactionData sendERC20Token(KeyPair keys,
                                   String contract,
                                   BigInteger gasPrice,
                                   String to,
                                   BigInteger value,
                                   BigInteger fee);

    BigInteger balance(KeyPair keyPair, String contract, BigInteger gasPrice);

}
