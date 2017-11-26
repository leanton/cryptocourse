package me.antonle.crypto.scrooge;

import java.util.ArrayList;
import java.util.HashMap;

public class UTXOPool {

    /**
     * The current collection of UTXOs, with each one mapped to its corresponding transaction output
     */
    private HashMap<UTXO, Transaction.Output> H;

    /**
     * Creates a new empty me.antonle.crypto.scrooge.UTXOPool
     */
    public UTXOPool() {
        H = new HashMap<>();
    }

    /**
     * Creates a new me.antonle.crypto.scrooge.UTXOPool that is a copy of {@code uPool}
     */
    public UTXOPool(UTXOPool uPool) {
        H = new HashMap<>(uPool.H);
    }

    /**
     * Adds a mapping from me.antonle.crypto.scrooge.UTXO {@code utxo} to transaction output @code{txOut} to the pool
     */
    public void addUTXO(UTXO utxo, Transaction.Output txOut) {
        H.put(utxo, txOut);
    }

    /**
     * Removes the me.antonle.crypto.scrooge.UTXO {@code utxo} from the pool
     */
    public void removeUTXO(UTXO utxo) {
        H.remove(utxo);
    }

    /**
     * @return the transaction output corresponding to me.antonle.crypto.scrooge.UTXO {@code utxo}, or null if {@code utxo} is
     * not in the pool.
     */
    public Transaction.Output getTxOutput(UTXO ut) {
        return H.get(ut);
    }

    /**
     * @return true if me.antonle.crypto.scrooge.UTXO {@code utxo} is in the pool and false otherwise
     */
    public boolean contains(UTXO utxo) {
        return H.containsKey(utxo);
    }

    /**
     * Returns an {@code ArrayList} of all UTXOs in the pool
     */
    public ArrayList<UTXO> getAllUTXO() {
        return new ArrayList<>(H.keySet());
    }
}
