package com.github.facade.ethrereum.iterator;

import com.github.facade.ethrereum.model.EthereumChainInfo;
import com.github.facade.ethrereum.repository.IEthereumRepository;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public interface BlockIterator extends Iterator<Optional<EthereumChainInfo>> {

    class Builder {

        private AtomicLong cursor;

        private long height;

        private int interval;

        public Builder height(long height) {
            this.height = height;
            return this;
        }

        public Builder cursor() {
            this.cursor = new AtomicLong(this.height);
            return this;
        }

        public Builder interval(int interval) {
            this.interval = interval;
            return this;
        }

        public BlockIterator iterator(IEthereumRepository repository) {
            return new DefaultIterator(
                    this.cursor,
                    this.interval,
                    repository
            );
        }

    }

}
