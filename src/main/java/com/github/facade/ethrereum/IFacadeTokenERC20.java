package com.github.facade.ethrereum;

import com.github.facade.ethrereum.model.KeyPair;
import com.github.facade.ethrereum.model.TransactionData;

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
