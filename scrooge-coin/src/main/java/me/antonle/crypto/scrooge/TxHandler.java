package me.antonle.crypto.scrooge;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the me.antonle.crypto.scrooge.UTXOPool(me.antonle.crypto.scrooge.UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // TODO: IMPLEMENT THIS
        throw new RuntimeException("Not implemented");
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no me.antonle.crypto.scrooge.UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // TODO: IMPLEMENT THIS
        throw new RuntimeException("Not implemented");
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current me.antonle.crypto.scrooge.UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // TODO: IMPLEMENT THIS
        throw new RuntimeException("Not implemented");
    }

}
