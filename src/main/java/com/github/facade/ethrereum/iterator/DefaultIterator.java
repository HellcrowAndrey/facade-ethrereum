package com.github.facade.ethrereum.iterator;

import com.github.facade.ethrereum.model.EthereumChainInfo;
import com.github.facade.ethrereum.repository.IEthereumRepository;
import com.github.facade.ethrereum.state.NextRequest;
import com.github.facade.ethrereum.state.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class DefaultIterator implements BlockIterator {

    private static final Logger log = LoggerFactory.getLogger(DefaultIterator.class);

    private final Executor delayExecutor;

    private final AtomicLong cursor;

    private final int timeout;

    private final IEthereumRepository repository;

    private final RequestContext context;

    public DefaultIterator(AtomicLong cursor, int interval, int timeout, IEthereumRepository repository) {
        this.cursor = cursor;
        this.delayExecutor = CompletableFuture.delayedExecutor(interval, TimeUnit.MILLISECONDS);
        this.timeout = timeout;
        this.repository = repository;
        this.context = new RequestContext(this.repository, this.delayExecutor, this.timeout, new NextRequest());
    }

    public DefaultIterator(AtomicLong cursor, int interval, IEthereumRepository repository) {
        this.cursor = cursor;
        this.delayExecutor = CompletableFuture.delayedExecutor(interval, TimeUnit.MILLISECONDS);
        this.timeout = 60;
        this.repository = repository;
        this.context = new RequestContext(this.repository, this.delayExecutor, this.timeout, new NextRequest());
    }

    @Override
    public boolean hasNext() {
        return this.cursor.get() < Long.MAX_VALUE;
    }

    @Override
    public Optional<EthereumChainInfo> next() {
        Optional<EthereumChainInfo> current = this.context.request(
                BigInteger.valueOf(this.cursor.incrementAndGet()
                ));
        return changeCursor().apply(current);
    }

    private Function<Optional<EthereumChainInfo>, Optional<EthereumChainInfo>> changeCursor() {
        return current -> {
            if (current.isEmpty()) {
                this.cursor.getAndDecrement();
            }
            return current;
        };
    }

}
