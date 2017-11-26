import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        for (int i = 0; i < tx.getInputs().size(); i++) {

            final Transaction.Input input = tx.getInput(i);
            final UTXO txUtxo = new UTXO(input.prevTxHash, input.outputIndex);

            // check 3 - no UTXO is claimed multiple times
            if (utxoSet.contains(txUtxo)) {
                return false;
            }
            utxoSet.add(txUtxo);

            final Transaction.Output prevTx = pool.getTxOutput(txUtxo);
            // check 1 - all output claimed by tx are in current UTXO pool
            if (!pool.contains(txUtxo)) {
                return false;
            }
            // check 2 - signatures of each input are valid
            if (input.signature == null || !Crypto.verifySignature(prevTx.address, tx.getRawDataToSign(i), input.signature)) {
                return false;
            }
            inputSum += prevTx.value;
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
        List<Transaction> transactions = new ArrayList<>();

        for (Transaction transaction : possibleTxs) {
            if (isValidTx(transaction)) {
                transactions.add(transaction);

                // do i need to delete all the utxo with signature prevTxHash?
                for (int i = 0; i < transaction.getInputs().size(); i++) {
                    final Transaction.Input input = transaction.getInput(i);
                    pool.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
                }

                for (int i = 0; i < transaction.getOutputs().size(); i++) {
                    final UTXO utxo = new UTXO(transaction.getHash(), i);
                    final Transaction.Output output = transaction.getOutput(i);
                    pool.addUTXO(utxo, output);
                }
            }
        }

        final Transaction[] result = new Transaction[transactions.size()];
        return transactions.toArray(result);
    }

}
