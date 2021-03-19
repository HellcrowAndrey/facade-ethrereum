package com.github.facade.ethrereum;

import com.github.facade.ethrereum.contracts.ERC20;
import com.github.facade.ethrereum.exceptions.BalanceException;
import com.github.facade.ethrereum.exceptions.BroadcastException;
import com.github.facade.ethrereum.exceptions.NonceException;
import com.github.facade.ethrereum.model.Information;
import com.github.facade.ethrereum.model.KeyPair;
import com.github.facade.ethrereum.model.TransactionData;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.web3j.protocol.core.DefaultBlockParameterName.PENDING;
import static org.web3j.tx.Transfer.GAS_LIMIT;

public final class FacadeEthereum implements IFacadeEthereum {

    private static final Logger log = LoggerFactory.getLogger(FacadeEthereum.class);

    private final Web3j web3j;

    private Subscription transactionSub;

    private Subscription blockSub;

    private int time;

    private AtomicLong count;

    public FacadeEthereum(String url) {
        this.web3j = Web3j.build(new HttpService(url));
    }

    public FacadeEthereum(String url, int time) {
        this.web3j = Web3j.build(new HttpService(url));
        this.time = time;
    }

    @Override
    public final KeyPair generateKeys() {
        try {
            ECKeyPair keys = Keys.createEcKeyPair();
            String address = Numeric.prependHexPrefix(Keys.getAddress(keys));
            return new KeyPair(keys.getPrivateKey(), keys.getPublicKey(), address);
        } catch (InvalidAlgorithmParameterException |
                NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error("Enter: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public final BigInteger nonce(String address) {
        try {
            EthGetTransactionCount response = this.web3j
                    .ethGetTransactionCount(address, PENDING).send();
            if (!response.hasError()) {
                return response.getTransactionCount();
            } else {
                throw new NonceException(
                        response.getError().getCode(),
                        response.getError().getMessage()
                );
            }
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
        }
        throw new NonceException("Can't get address nonce!");
    }

    @Override
    public final TransactionData
    send(KeyPair keys, BigInteger gasPrice, String to, BigInteger value, BigInteger fee) {
        BigInteger nonce = nonce(keys.getAddress());
        Credentials credentials = Credentials.create(
                new ECKeyPair(keys.getPrivateKey(), keys.getPublicKey())
        );
        RawTransaction transaction = RawTransaction.createTransaction(
                nonce, gasPrice, GAS_LIMIT, to, value, ""
        );
        return send(
                credentials, transaction, nonce,
                gasPrice, keys.getAddress(), to, value, fee
        );
    }

    private TransactionData send(Credentials credentials, RawTransaction transaction,
                                 BigInteger nonce, BigInteger gasPrice, String from,
                                 String to, BigInteger value, BigInteger fee) {
        try {
            RawTransactionManager manager = new RawTransactionManager(this.web3j, credentials);
            EthSendTransaction response = manager.signAndSend(transaction);
            if (!response.hasError()) {
                return new TransactionData(
                        response.getTransactionHash(), nonce,
                        "", BigInteger.ZERO, gasPrice,
                        GAS_LIMIT, from, to, value, fee, ""
                );
            } else {
                Response.Error err = response.getError();
                throw new BroadcastException(err.getCode(), err.getMessage());
            }
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
            throw new BroadcastException(-1, e.getMessage());
        }
    }

    @Override
    public final BigInteger fee(BigInteger gasPrice) {
        return gasPrice.multiply(GAS_LIMIT);
    }

    @Override
    public final BigInteger gasPrice() throws IOException {
        EthGasPrice response = this.web3j.ethGasPrice().send();
        return !response.hasError() ? response.getGasPrice() : null;
    }

    @Override
    public final BigInteger balance(String address) {
        try {
            EthGetBalance response = this.web3j.ethGetBalance(
                    address,
                    DefaultBlockParameterName.LATEST
            ).send();
            if (!response.hasError()) {
                return response.getBalance();
            } else {
                Response.Error err = response.getError();
                throw new BalanceException(err.getCode(), err.getMessage());
            }
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
            throw new BalanceException(-1, "Can't get balance by address");
        }
    }

    @Override
    public final void startInfoTrack(Consumer<Information> information, Consumer<Throwable> errors) {
        this.blockSub = (Subscription) this.web3j.blockFlowable(Boolean.FALSE).subscribe(
                b -> this.information(information, errors),
                e -> Observable.just(e).subscribe(errors).dispose()
        );
    }

    private void information(Consumer<Information> information, Consumer<Throwable> errors) {
        try {
            BigInteger gasPrice = this.gasPrice();
            if (Objects.nonNull(gasPrice)) {
                BigInteger fee = this.fee(gasPrice);
                Information info = new Information(fee, gasPrice);
                Observable.just(info)
                        .subscribe(information, errors)
                        .dispose();
            }
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
        }
    }

    @Override
    public final void restartInfoTrack(Consumer<Information> information, Consumer<Throwable> errors) {
        this.blockSub.cancel();
        startInfoTrack(information, errors);
    }

    @Override
    public final void startTransactionTrack(
            Consumer<TransactionData> incomingEth,
            Consumer<TransactionData> outgoingEth,
            Consumer<TransactionData> incomingContract,
            Consumer<TransactionData> outgoingContract,
            Consumer<Throwable> errors,
            Supplier<List<String>> usersAddresses,
            Supplier<List<String>> contractsAddresses) {
        this.transactionSub = (Subscription) this.web3j.transactionFlowable().subscribe(
                tx -> transactions(
                        tx, incomingEth, outgoingEth,
                        incomingContract, outgoingContract,
                        errors, usersAddresses, contractsAddresses
                ),
                e -> Observable.just(e).subscribe(errors).dispose()
        );
    }

    private void transactions(Transaction tx,
                              Consumer<TransactionData> incomingEth,
                              Consumer<TransactionData> outgoingEth,
                              Consumer<TransactionData> incomingContract,
                              Consumer<TransactionData> outgoingContract,
                              Consumer<Throwable> errors,
                              Supplier<List<String>> usersAddresses,
                              Supplier<List<String>> contractsAddresses) {
        List<String> addresses = usersAddresses.get();
        List<String> contracts = contractsAddresses.get();
        transactionsHandler(tx, incomingEth, outgoingEth, errors, addresses, contracts);
        contractsHandler(tx, incomingContract, outgoingContract, errors, addresses, contracts);
    }

    private void transactionsHandler(Transaction tx,
                                     Consumer<TransactionData> incomingEth,
                                     Consumer<TransactionData> outgoingEth,
                                     Consumer<Throwable> errors,
                                     List<String> addresses,
                                     List<String> contracts) {
        Observable.just(tx)
                .filter(trx -> !contracts.contains(trx.getTo()))
                .filter(trx -> addresses.contains(trx.getTo()))
                .map(this::toTransaction)
                .subscribe(incomingEth, errors)
                .dispose();
        Observable.just(tx)
                .filter(trx -> !contracts.contains(trx.getTo()))
                .filter(trx -> addresses.contains(trx.getFrom()))
                .map(this::toTransaction)
                .subscribe(outgoingEth, errors)
                .dispose();
    }

    private void contractsHandler(Transaction tx,
                                  Consumer<TransactionData> incomingContract,
                                  Consumer<TransactionData> outgoingContract,
                                  Consumer<Throwable> errors,
                                  List<String> addresses,
                                  List<String> contracts) {
        Observable.just(tx)
                .filter(contract -> contracts.contains(contract.getTo()))
                .map(this::toContract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(trx -> addresses.contains(trx.getTo()))
                .subscribe(incomingContract, errors)
                .dispose();
        Observable.just(tx)
                .filter(contract -> contracts.contains(contract.getTo()))
                .map(this::toContract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(trx -> addresses.contains(trx.getFrom()))
                .subscribe(outgoingContract, errors)
                .dispose();
    }

    private TransactionData toTransaction(Transaction tx) {
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

    private Optional<TransactionData> toContract(Transaction tx)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String inputData = tx.getInput();
        String addressTo = inputData.substring(10, 74);
        String value = inputData.substring(74);
        Method refMethod = TypeDecoder.class.getDeclaredMethod(
                "decode", String.class, int.class, Class.class
        );
        refMethod.setAccessible(Boolean.TRUE);
        Address address = (Address) refMethod.invoke(null, addressTo, 0, Address.class);
        Uint256 amount = (Uint256) refMethod.invoke(null, value, 0, Uint256.class);
        BigInteger gasPrice = tx.getGasPrice();
        BigInteger fee = gasPrice.multiply(GAS_LIMIT);
        return Optional.of(new TransactionData(
                tx.getHash(), tx.getNonce(),
                tx.getBlockHash(), tx.getBlockNumber(),
                gasPrice, GAS_LIMIT,
                tx.getTo(), tx.getFrom(),
                address.toString(),
                amount.getValue(), fee
        ));
    }

    @Override
    public final void restartTransactionTrack(Consumer<TransactionData> incomingEth,
                                              Consumer<TransactionData> outgoingEth,
                                              Consumer<TransactionData> incomingContract,
                                              Consumer<TransactionData> outgoingContract,
                                              Consumer<Throwable> errors,
                                              Supplier<List<String>> usersAddresses,
                                              Supplier<List<String>> contractsAddresses) {
        this.transactionSub.cancel();
        startTransactionTrack(
                incomingEth, outgoingEth,
                incomingContract, outgoingContract,
                errors, usersAddresses, contractsAddresses
        );
    }

    @Override
    public BigInteger toWei(BigDecimal value) {
        return Convert.toWei(value, Convert.Unit.ETHER)
                .toBigInteger();
    }

    @Override
    public BigDecimal fromWei(BigInteger value) {
        return Convert.fromWei(value.toString(), Convert.Unit.ETHER);
    }

    @Override
    public void blockTracker(
            Supplier<Long> blockNumber,
            Consumer<TransactionData> incomingEth,
            Consumer<TransactionData> outgoingEth,
            Consumer<TransactionData> incomingContract,
            Consumer<TransactionData> outgoingContract,
            Supplier<List<String>> usersAddresses,
            Supplier<List<String>> contractsAddresses,
            Consumer<Information> information,
            Consumer<Long> currentBlockNumber,
            Consumer<Throwable> errors) {
        this.count = new AtomicLong(blockNumber.get());
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> startBlockTracker(
                        incomingEth, outgoingEth,
                        incomingContract, outgoingContract,
                        usersAddresses, contractsAddresses,
                        information, currentBlockNumber, errors
                        ),
                        BigInteger.ZERO.intValue(),
                        this.time,
                        TimeUnit.SECONDS
                );
    }

    @Override
    public Optional<Long> bastBlock() {
        try {
            EthBlock response = this.web3j
                    .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, Boolean.TRUE)
                    .send();
            if (!response.hasError()) {
                return Optional.of(response.getBlock().getNumber().longValue());
            }
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Pair input(String input) {
        StringBuilder sb = new StringBuilder(input);
        String addressTo = sb.substring(10, 74);
        String value = sb.substring(74);
        try {
            Method refMethod = TypeDecoder.class.getDeclaredMethod(
                    "decode", String.class, int.class, Class.class
            );
            refMethod.setAccessible(Boolean.TRUE);
            Address address = (Address) refMethod.invoke(null, addressTo, 0, Address.class);
            Uint256 amount = (Uint256) refMethod.invoke(null, value, 0, Uint256.class);
            return new Pair(amount.toString(), amount.getValue());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.warn("Enter: {}", e.getMessage());
        }
        return null;
    }

    private void startBlockTracker(
            Consumer<TransactionData> incomingEth,
            Consumer<TransactionData> outgoingEth,
            Consumer<TransactionData> incomingContract,
            Consumer<TransactionData> outgoingContract,
            Supplier<List<String>> usersAddresses,
            Supplier<List<String>> contractsAddresses,
            Consumer<Information> information,
            Consumer<Long> currentBlockNumber,
            Consumer<Throwable> errors
    ) {
        fetchBlock(this.count.incrementAndGet(), currentBlockNumber, errors)
                .ifPresentOrElse(
                        block -> blockHandler(
                                block,
                                incomingEth,
                                outgoingEth,
                                incomingContract,
                                outgoingContract,
                                usersAddresses,
                                contractsAddresses,
                                information,
                                errors
                        ),
                        () -> this.count.decrementAndGet()
                );
    }

    private void blockHandler(EthBlock.Block block,
                              Consumer<TransactionData> incomingEth,
                              Consumer<TransactionData> outgoingEth,
                              Consumer<TransactionData> incomingContract,
                              Consumer<TransactionData> outgoingContract,
                              Supplier<List<String>> usersAddresses,
                              Supplier<List<String>> contractsAddresses,
                              Consumer<Information> information,
                              Consumer<Throwable> errors) {
        List<EthBlock.TransactionResult> transactions = block.getTransactions();
        transactions.stream()
                .map(t -> cast(t.get()))
                .filter(Objects::nonNull)
                .forEach(t -> transactions(
                        t, incomingEth, outgoingEth,
                        incomingContract, outgoingContract,
                        usersAddresses, contractsAddresses, errors
                        )
                );
        information(information, errors);
    }

    private Optional<EthBlock.Block> fetchBlock(Long number,
                                                Consumer<Long> currentBlockNumber,
                                                Consumer<Throwable> errors) {
        EthBlock.Block block = null;
        EthBlock response = blockRequest(BigInteger.valueOf(number));
        if (Objects.nonNull(response)) {
            block = response.getBlock();
            Observable.just(number)
                    .subscribe(currentBlockNumber, errors)
                    .dispose();
        }
        return Optional.ofNullable(block);
    }

    private EthBlock blockRequest(BigInteger number) {
        try {
            return this.web3j
                    .ethGetBlockByNumber(DefaultBlockParameter.valueOf(number), Boolean.TRUE)
                    .send();
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
        }
        return null;
    }

    private void transactions(EthBlock.TransactionObject tx,
                              Consumer<TransactionData> incomingEth,
                              Consumer<TransactionData> outgoingEth,
                              Consumer<TransactionData> incomingContract,
                              Consumer<TransactionData> outgoingContract,
                              Supplier<List<String>> usersAddresses,
                              Supplier<List<String>> contractsAddresses,
                              Consumer<Throwable> errors) {
        List<String> addresses = usersAddresses.get();
        List<String> contracts = contractsAddresses.get();
        transactionsHandler(tx, incomingEth, outgoingEth, errors, addresses, contracts);
        contractsHandler(tx, incomingContract, outgoingContract, errors, addresses, contracts);
    }

    private void transactionsHandler(EthBlock.TransactionObject tx,
                                     Consumer<TransactionData> incomingEth,
                                     Consumer<TransactionData> outgoingEth,
                                     Consumer<Throwable> errors,
                                     List<String> addresses,
                                     List<String> contracts) {
        Observable.just(tx)
                .filter(trx -> !contracts.contains(trx.getTo()))
                .filter(trx -> addresses.contains(trx.getTo()))
                .map(this::toTransaction)
                .subscribe(incomingEth, errors)
                .dispose();
        Observable.just(tx)
                .filter(trx -> !contracts.contains(trx.getTo()))
                .filter(trx -> addresses.contains(trx.getFrom()))
                .map(this::toTransaction)
                .subscribe(outgoingEth, errors)
                .dispose();
    }

    private void contractsHandler(EthBlock.TransactionObject tx,
                                  Consumer<TransactionData> incomingContract,
                                  Consumer<TransactionData> outgoingContract,
                                  Consumer<Throwable> errors,
                                  List<String> addresses,
                                  List<String> contracts) {
        Observable.just(tx)
                .filter(contract -> contracts.contains(contract.getTo()))
                .map(this::toContract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(trx -> addresses.contains(trx.getTo()))
                .subscribe(incomingContract, errors)
                .dispose();
        Observable.just(tx)
                .filter(contract -> contracts.contains(contract.getTo()))
                .map(this::toContract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(trx -> addresses.contains(trx.getFrom()))
                .subscribe(outgoingContract, errors)
                .dispose();
    }

    private TransactionData toTransaction(EthBlock.TransactionObject tx) {
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

    private ERC20 load(String contract, Credentials credentials, BigInteger gasPrice) {
        ContractGasProvider cgp = new StaticGasProvider(gasPrice, GAS_LIMIT);
        TransactionManager tm = new RawTransactionManager(this.web3j, credentials);
        return ERC20.load(contract, this.web3j, tm, cgp);
    }

    @Override
    public TransactionData sendERC20Token(
            KeyPair keys, String contract,
            BigInteger gasPrice, String to,
            BigInteger value, BigInteger fee) {
        BigInteger nonce = nonce(keys.getAddress());
        Credentials credentials = Credentials.create(
                new ECKeyPair(keys.getPrivateKey(), keys.getPublicKey())
        );
        ERC20 con = load(contract, credentials, gasPrice);
        return sendERC20Token(
                con, nonce, gasPrice, contract,
                credentials.getAddress(),
                to, value, fee
        );
    }

    @Override
    public BigInteger balance(KeyPair keys, String contract, BigInteger gasPrice) {
        Credentials credentials = Credentials.create(
                new ECKeyPair(keys.getPrivateKey(), keys.getPublicKey())
        );
        ERC20 con = load(contract, credentials, gasPrice);
        try {
            return con.balanceOf(credentials.getAddress()).send();
        } catch (Exception e) {
            log.warn("Enter: {}", e.getMessage());
            throw new BalanceException(-1, e.getMessage());
        }
    }

    public TransactionData
    sendERC20Token(ERC20 contract, BigInteger nonce,
                   BigInteger gasPrice, String addressContract,
                   String from, String to, BigInteger value, BigInteger fee) {
        try {
            TransactionReceipt response = contract.transfer(to, value).send();
            if (response.isStatusOK()) {
                return new TransactionData(
                        response.getTransactionHash(), nonce,
                        response.getBlockHash(), response.getBlockNumber(),
                        gasPrice, GAS_LIMIT, addressContract,
                        from, to, value, fee
                );
            } else {
                throw new BroadcastException(400, response.getStatus());
            }
        } catch (Exception e) {
            log.warn("Enter: {}", e.getMessage());
            throw new BroadcastException(-1, "Can't send contract.");
        }
    }

    private EthBlock.TransactionObject cast(Object t) {
        return (EthBlock.TransactionObject) t;
    }

}
