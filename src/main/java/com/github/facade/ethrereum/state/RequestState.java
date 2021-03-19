package com.github.facade.ethrereum.state;

import com.github.facade.ethrereum.model.EthereumChainInfo;

import java.math.BigInteger;
import java.util.Optional;

public interface RequestState {
    Optional<EthereumChainInfo> requests(BigInteger height, RequestContext context);
}
