package com.github.facade.ethrereum.iterator;

import com.github.facade.ethrereum.model.EthereumChainInfo;
import com.github.facade.ethrereum.repository.IEthereumRepository;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BlockStream {

    private BlockStream() {}

    public static Stream<EthereumChainInfo> stream(long height, int interval, IEthereumRepository repository) {
        BlockIterator iter = new BlockIterator.Builder()
                .height(height)
                .cursor()
                .interval(interval)
                .iterator(repository);
        return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iter, Spliterator.DISTINCT),
                    false
            ).filter(Optional::isPresent).map(Optional::get);
    }

    public static Stream<EthereumChainInfo> stream(long height, IEthereumRepository repository) {
        BlockIterator iter = new BlockIterator.Builder()
                .height(height)
                .cursor()
                .interval(1000)
                .iterator(repository);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iter, Spliterator.DISTINCT),
                false
        ).filter(Optional::isPresent).map(Optional::get);
    }

}
