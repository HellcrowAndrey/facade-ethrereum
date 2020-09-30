package com.github.wrapper.ethrereum.facade.impl;

import com.github.wrapper.ethrereum.facade.IFacadeEthereum;
import com.github.wrapper.ethrereum.model.KeyPair;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.http.HttpService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FacadeEthereumTest {

    private IFacadeEthereum facade;

    @Before
    public void setUp() {
        this.facade = new FacadeEthereum(HttpService.DEFAULT_URL);
    }

    @Test
    public void generateKeys() {
        KeyPair keys = this.facade.generateKeys();
        assertNotNull(keys);
        assertNotNull(keys.getPrivateKey());
        assertNotNull(keys.getPublicKey());
        assertNotNull(keys.getAddress());
    }

    @Test
    public void generateAddress() {
        KeyPair keyPair = FacadeEthereumMocks.keyPair();
        Credentials credentials = Credentials.create(
                new ECKeyPair(keyPair.getPrivateKey(), keyPair.getPublicKey())
        );
        assertEquals(keyPair.getAddress(), credentials.getAddress());
    }

}