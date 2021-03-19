package com.github.facade.ethrereum.repository;

import com.github.facade.ethrereum.model.EthereumBlock;

import java.math.BigInteger;

public interface IEthereumRepository {

    BigInteger gasPrice();

    EthereumBlock blockByNumber(BigInteger number);

}
