package com.github.facade.ethrereum.state;

import com.github.facade.ethrereum.exceptions.ConnectionRefusedException;
import com.github.facade.ethrereum.model.EthereumChainInfo;
import com.github.facade.ethrereum.repository.impl.EthereumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.*;

public class RestoreConnectRequest implements RequestState {

    private static final Logger log = LoggerFactory.getLogger(EthereumRepository.class);

    private final Executor delayExecutor;

    private final int timeout;

    private RestoreConnectRequest(Executor delayExecutor) {
        this.delayExecutor = delayExecutor;
        this.timeout = 120;
    }

    static RestoreConnectRequest create() {
        Executor delayExecutor =  CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS);
        return new RestoreConnectRequest(delayExecutor);
    }

    @Override
    public Optional<EthereumChainInfo> requests(BigInteger height, RequestContext context) {
        Optional<EthereumChainInfo> resp = Optional.empty();
        try {
            resp = CompletableFuture.supplyAsync(
                    () -> context.collector(height), this.delayExecutor)
                    .get(this.timeout, TimeUnit.SECONDS);
        } catch (ConnectionRefusedException | InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Enter: Bad connection {}", e.getMessage());
        }
        if (resp.isPresent()) {
            context.changeState(new NextRequest());
        }
        return resp;
    }

}
