package com.github.facade.ethrereum.state;

import com.github.facade.ethrereum.model.EthereumBlock;
import com.github.facade.ethrereum.model.EthereumChainInfo;
import com.github.facade.ethrereum.repository.IEthereumRepository;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

public class RequestContext {

    private final Executor delayExecutor;

    private final int timeout;

    private final IEthereumRepository repository;

    private RequestState state;

    public RequestContext(IEthereumRepository repository, Executor delayExecutor, int timeout, RequestState state) {
        this.repository = repository;
        this.state = state;
        this.delayExecutor = delayExecutor;
        this.timeout = timeout;
    }

    void changeState(RequestState state) {
        this.state = state;
    }

    Executor getDelayExecutor() {
        return delayExecutor;
    }

    int getTimeout() {
        return timeout;
    }

    Optional<EthereumChainInfo> collector(BigInteger height) {
        EthereumBlock block = this.repository.blockByNumber(height);
        BigInteger gasPrice = this.repository.gasPrice();
        if (Objects.nonNull(block)) {
            if (Objects.nonNull(gasPrice)) {
                return Optional.of(new EthereumChainInfo(block, gasPrice));
            }
        }
        return Optional.empty();
    }

    public Optional<EthereumChainInfo> request(BigInteger height) {
        return this.state.requests(height, this);
    }

}
