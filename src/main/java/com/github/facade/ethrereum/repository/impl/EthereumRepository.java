package com.github.facade.ethrereum.repository.impl;

import com.github.facade.ethrereum.exceptions.ConnectionRefusedException;
import com.github.facade.ethrereum.model.EthereumBlock;
import com.github.facade.ethrereum.repository.IEthereumRepository;
import com.github.facade.ethrereum.utils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGasPrice;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EthereumRepository implements IEthereumRepository {

    private static final Logger log = LoggerFactory.getLogger(EthereumRepository.class);

    private final Web3j web3j;

    public EthereumRepository(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public BigInteger gasPrice() {
        BigInteger result = null;
        try {
            EthGasPrice response = this.web3j.ethGasPrice().send();
            if (!response.hasError()) {
               result = response.getGasPrice();
            }
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
            throw new ConnectionRefusedException();
        }
        return result;
    }

    @Override
    public EthereumBlock blockByNumber(BigInteger number) {
        try {
            EthBlock resp = this.web3j
                    .ethGetBlockByNumber(DefaultBlockParameter.valueOf(number), Boolean.TRUE)
                    .send();
            if (Objects.nonNull(resp)) {
                EthBlock.Block block = resp.getBlock();
                if (Objects.nonNull(block)) {
                    List<EthBlock.TransactionResult> transactions = block.getTransactions();
                    return new EthereumBlock(
                            block.getHash(),
                            block.getNumber(),
                            block.getNonce(),
                            block.getSize(),
                            block.getGasLimit(),
                            block.getGasUsed(),
                            transactions.stream()
                                    .map(t -> (EthBlock.TransactionObject) t.get())
                                    .filter(Objects::nonNull)
                                    .map(TransactionUtils::toTransaction)
                                    .collect(Collectors.toList())
                    );
                }
            }
        } catch (IOException e) {
            log.warn("Enter: {}", e.getMessage());
            throw new ConnectionRefusedException();
        }
        return null;
    }

}
