package com.github.facade.ethrereum.facade.impl;

import com.github.facade.ethrereum.FacadeEthereum;
import com.github.facade.ethrereum.IFacadeEthereum;
import com.github.facade.ethrereum.model.KeyPair;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FacadeEthereumTest {

    private IFacadeEthereum facade;

    @Before
    public void setUp() {
        this.facade = new FacadeEthereum(
                "https://mainnet.infura.io/v3/bb5a88796ab14494bb1bf9173d4ce271",
                10
        );
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