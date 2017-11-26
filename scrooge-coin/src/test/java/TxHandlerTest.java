import org.junit.Before;
import org.junit.Test;

import java.security.*;

import static org.junit.Assert.*;

public class TxHandlerTest {
    private KeyPair scroogeKeyPair;
    private KeyPairGenerator keyGen;
    private Transaction firstTxn;
    private UTXOPool pool;
    private Signature sig;
    private MessageDigest md;
    private KeyPair goofy;
    private KeyPair mickey;

    private KeyPair genKey() {
        return keyGen.generateKeyPair();
    }

    @Before
    public void setUp() throws NoSuchAlgorithmException, SignatureException {
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);

        // This hashes stuff
        md = MessageDigest.getInstance("SHA-512");
        // This creates signatures
        sig = Signature.getInstance("SHA256withRSA");

        // Scrooge generates a key pair
        keyGen.initialize(512);
        scroogeKeyPair = keyGen.generateKeyPair();

        // Creates genesis transaction
        firstTxn = new Transaction();
        firstTxn.addOutput(100, scroogeKeyPair.getPublic());

        // Hashes it
        firstTxn.setHash(md.digest(firstTxn.getRawTx()));

        // Adds it to the pool
        pool = new UTXOPool();
        final UTXO utxo = new UTXO(firstTxn.getHash(), 0);
        pool.addUTXO(utxo, firstTxn.getOutput(0));

        // Gooofy and Mickey key pairs
        goofy = genKey();
        mickey = genKey();

    }

    @Test
    public void testIsValidTx() throws SignatureException, InvalidKeyException {

        //Scrooge makes a transaction to Goofy
        Transaction send = new Transaction();
        send.addInput(firstTxn.getHash(), 0);
        send.addOutput(50, goofy.getPublic());
        send.addOutput(50, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(scroogeKeyPair.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertTrue(handler.isValidTx(send));
    }

    @Test
    public void testNegTrans() throws SignatureException, InvalidKeyException {
        //Scrooge makes a transaction to Goofy
        Transaction send = new Transaction();
        send.addInput(firstTxn.getHash(), 0);
        send.addOutput(50, goofy.getPublic());
        send.addOutput(-1, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(scroogeKeyPair.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertFalse(handler.isValidTx(send));
    }

    /**
     * Test 3: test isValidTx() with transactions containing signatures using incorrect private keys
     * ==> FAILED
     **/
    @Test
    public void testBadSigs() throws SignatureException, InvalidKeyException {
        // Scrooge makes a transaction to Goofy
        Transaction send = new Transaction();
        send.addInput(firstTxn.getHash(), 0);
        send.addOutput(50, goofy.getPublic());
        send.addOutput(-1, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(goofy.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertFalse(handler.isValidTx(send));
    }

    @Test
    public void testOverLimit() throws InvalidKeyException, SignatureException {
        // Scrooge makes a transaction to Goofy
        Transaction send = new Transaction();
        send.addInput(firstTxn.getHash(), 0);
        send.addOutput(50, goofy.getPublic());
        send.addOutput(1000, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(goofy.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertFalse(handler.isValidTx(send));
    }

    @Test
    public void testOutputNotInPool() throws InvalidKeyException, SignatureException {
        Transaction bogus = new Transaction();

        // Scrooge makes a transaction to Goofy
        Transaction send = new Transaction();
        send.addInput(bogus.getHash(), 0);
        send.addOutput(50, goofy.getPublic());
        send.addOutput(1000, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(goofy.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertFalse(handler.isValidTx(send));
    }

    @Test
    public void testNegValues() throws InvalidKeyException, SignatureException {
        // Scrooge makes a transaction to Goofy
        Transaction send = new Transaction();
        send.addInput(firstTxn.getHash(), 0);
        send.addOutput(-1, goofy.getPublic());
        send.addOutput(101, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(scroogeKeyPair.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertFalse(handler.isValidTx(send));
    }

    @Test
    public void testMultipleClaims() throws InvalidKeyException, SignatureException {

        //Scrooge makes a transaction to Goofy
        Transaction send = new Transaction();
        send.addInput(firstTxn.getHash(), 0);
        send.addInput(firstTxn.getHash(), 1);
        send.addOutput(-1, goofy.getPublic());
        send.addOutput(101, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(scroogeKeyPair.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertFalse(handler.isValidTx(send));
    }

    @Test
    public void testMultipleTxns() throws InvalidKeyException, SignatureException {
        // Scrooge makes a transaction to Goofy and Mickey
        final Transaction send = new Transaction();
        send.addInput(firstTxn.getHash(), 0);
        send.addOutput(50, goofy.getPublic());
        send.addOutput(50, mickey.getPublic());

        // Signs the input with his private key
        sig.initSign(scroogeKeyPair.getPrivate());
        sig.update(send.getRawDataToSign(0));
        send.addSignature(sig.sign(), 0);

        // Hashes
        send.setHash(md.digest(send.getRawTx()));

        TxHandler handler = new TxHandler(pool);

        assertTrue(handler.isValidTx(send));

        // Next transaction references this one
        final Transaction next = new Transaction();
        next.addInput(send.getHash(), 0);
        next.addInput(send.getHash(), 1);
        next.addOutput(25, mickey.getPublic());
        next.addOutput(25, goofy.getPublic());

        sig.initSign(goofy.getPrivate());
        sig.update(next.getRawDataToSign(0));
        next.addSignature(sig.sign(), 0);

        sig.initSign(mickey.getPrivate());
        sig.update(next.getRawDataToSign(1));
        next.addSignature(sig.sign(), 1);

        next.setHash(md.digest(next.getRawTx()));

        final Transaction[] trans = new Transaction[2];
        trans[0] = send;
        trans[1] = next;

        assertEquals(2, handler.handleTxs(trans).length);
    }
}