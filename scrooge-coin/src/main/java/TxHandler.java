import java.util.*;
import java.util.stream.IntStream;

public class TxHandler {

    private final UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}.
     * This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        Set<UTXO> utxoSet = new HashSet<>();
        double inputSum = 0;
        for (int i = 0; i < tx.numInputs(); i++) {

            final Transaction.Input input = tx.getInput(i);
            if (Objects.isNull(input.prevTxHash)) {
                return false;
            }
            final UTXO txUtxo = new UTXO(input.prevTxHash, input.outputIndex);

            // check 3 - no UTXO is claimed multiple times
            if (utxoSet.contains(txUtxo)) {
                return false;
            }
            utxoSet.add(txUtxo);

            final Transaction.Output prevTxOutput = pool.getTxOutput(txUtxo);
            // check 1 - all output claimed by tx are in current UTXO pool
            if (!pool.contains(txUtxo)) {
                return false;
            }
            // check 2 - signatures of each input are valid
            if (input.signature == null || !Crypto.verifySignature(prevTxOutput.address, tx.getRawDataToSign(i), input.signature)) {
                return false;
            }
            inputSum += prevTxOutput.value;
        }

        // check 4 - non negative output values
        if (tx.getOutputs().stream().anyMatch(output -> output.value < 0)) {
            return false;
        }

        final double outputSum = tx.getOutputs().stream().mapToDouble(output -> output.value).sum();

        // check 5 - validating input values >= sum of output values
        return inputSum >= outputSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        return Arrays.stream(possibleTxs)
                .filter(this::isValidTx)
                .map(this::removeInputs)
                .map(this::addOutputs)
                .toArray(Transaction[]::new);
    }

    private Transaction addOutputs(Transaction transaction) {
        IntStream.range(0, transaction.numOutputs())
                .mapToObj(i -> new Tuple(transaction, i))
                .forEach(tuple -> pool.addUTXO(tuple.utxo, tuple.output));
        return transaction;
    }

    private Transaction removeInputs(Transaction transaction) {
        transaction.getInputs().stream()
                .map(input -> new UTXO(input.prevTxHash, input.outputIndex))
                .forEach(pool::removeUTXO);
        return transaction;
    }

    private static class Tuple {
        final UTXO utxo;
        final Transaction.Output output;

        Tuple(Transaction txn, int index) {
            this.utxo = new UTXO(txn.getHash(), index);
            this.output = txn.getOutput(index);
        }
    }

}
