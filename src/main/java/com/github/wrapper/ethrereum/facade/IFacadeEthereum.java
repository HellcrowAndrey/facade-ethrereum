package com.github.wrapper.ethrereum.facade;

import com.github.wrapper.ethrereum.model.Information;
import com.github.wrapper.ethrereum.model.KeyPair;
import com.github.wrapper.ethrereum.model.Response;
import com.github.wrapper.ethrereum.model.TransactionData;
import io.reactivex.functions.Consumer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

public interface IFacadeEthereum extends IFacadeTokenERC20 {

    KeyPair generateKeys();

    BigInteger nonce(String address);

    TransactionData send(
            KeyPair keys,
            BigInteger gasPrice,
            String to,
            BigInteger value,
            BigInteger fee
    );

    BigInteger fee(BigInteger gasPrice);

    BigInteger gasPrice() throws IOException;

    BigInteger balance(String address);

    void startInfoTrack(
            Consumer<Information> information,
            Consumer<Throwable> errors
    );

    void restartInfoTrack(
            Consumer<Information> information,
            Consumer<Throwable> errors
    );

    void startTransactionTrack(
            Consumer<TransactionData> incomingEth,
            Consumer<TransactionData> outgoingEth,
            Consumer<TransactionData> incomingContract,
            Consumer<TransactionData> outgoingContract,
            Consumer<Throwable> errors,
            Supplier<List<String>> usersAddresses,
            Supplier<List<String>> contractsAddresses
    );

    void restartTransactionTrack(
            Consumer<TransactionData> incomingEth,
            Consumer<TransactionData> outgoingEth,
            Consumer<TransactionData> incomingContract,
            Consumer<TransactionData> outgoingContract,
            Consumer<Throwable> errors,
            Supplier<List<String>> usersAddresses,
            Supplier<List<String>> contractsAddresses
    );

    BigInteger toWei(BigDecimal value);

    BigDecimal fromWei(BigInteger value);



}
