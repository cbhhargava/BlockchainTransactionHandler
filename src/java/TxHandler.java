import java.util.HashSet;

public class TxHandler {

    UTXOPool uPool;
    
    public TxHandler(UTXOPool uPool){
        this.uPool = new UTXOPool(uPool);
    }

    public boolean isValidTx(Transaction tx) {
        double oldTxSum = 0.0;
        double newTxSum = 0.0;
        UTXOPool vPool = new UTXOPool();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output output = uPool.getTxOutput(utxo);
            if (!uPool.contains(utxo)) return false;
            if(!output.address.verifySignature(tx.getRawDataToSign(i), in.signature)) return false;
            if (vPool.contains(utxo)) return false;
            vPool.addUTXO(utxo, output);
            oldTxSum += output.value;
        }
        for (Transaction.Output out : tx.getOutputs()) {
            if (out.value < 0) return false;
            newTxSum += out.value;
        }
        return oldTxSum >= newTxSum;
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        HashSet<Transaction> finalTxns = new HashSet<>();
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                finalTxns.add(tx);
                for (Transaction.Input in : tx.getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    uPool.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    uPool.addUTXO(utxo, out);
                }
            }
        }
        Transaction[] arr = new Transaction[finalTxns.size()];
        return finalTxns.toArray(arr);
    }

}
