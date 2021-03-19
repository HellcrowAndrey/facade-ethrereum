package com.github.facade.ethrereum.utils;

import com.github.facade.ethrereum.model.TransactionData;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;

import static org.web3j.tx.Transfer.GAS_LIMIT;

public class TransactionUtils {

    public static TransactionData toTransaction(EthBlock.TransactionObject tx) {
        BigInteger gasPrice = tx.getGasPrice();
        BigInteger fee = gasPrice.multiply(GAS_LIMIT);
        return new TransactionData(
                tx.getHash(), tx.getNonce(),
                tx.getBlockHash(), tx.getBlockNumber(),
                gasPrice, GAS_LIMIT,
                tx.getFrom(), tx.getTo(),
                tx.getValue(), fee, tx.getInput()
        );
    }

}
