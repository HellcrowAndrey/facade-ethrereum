package com.github.facade.ethrereum.state;

import com.github.facade.ethrereum.exceptions.ConnectionRefusedException;
import com.github.facade.ethrereum.model.EthereumChainInfo;
import com.github.facade.ethrereum.repository.impl.EthereumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.*;

public class NextRequest implements RequestState {

    private static final Logger log = LoggerFactory.getLogger(EthereumRepository.class);

    @Override
    public Optional<EthereumChainInfo> requests(BigInteger height, RequestContext context) {
        Optional<EthereumChainInfo> resp = Optional.empty();
        try {
            resp = CompletableFuture.supplyAsync(
                    () -> context.collector(height), context.getDelayExecutor())
                    .get(context.getTimeout(), TimeUnit.SECONDS);
        } catch (ConnectionRefusedException | InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Enter: {}", e.getMessage());
            context.changeState(RestoreConnectRequest.create());
        }
        return resp;
    }

}
