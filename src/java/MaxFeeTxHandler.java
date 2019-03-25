import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class MaxFeeTxHandler extends  TxHandler{

    UTXOPool uPool;

    public MaxFeeTxHandler(UTXOPool uPool){
        super(uPool);
        this.uPool = new UTXOPool(uPool);
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs){
        Comparator<Transaction> comp = new Comparator<Transaction>() {
            public int compare(Transaction t1, Transaction t2) {
                double t1Fee = fee(t1);
                double t2Fee = fee(t2);
                return Double.compare(t2Fee, t1Fee);
            }
        };
        ArrayList<Transaction> list = new ArrayList<Transaction>(Arrays.asList(possibleTxs));
        Collections.sort(list, comp);
        return super.handleTxs(list.toArray(possibleTxs));
    }

    private double fee(Transaction tx) {
        double sumInputs = 0;
        double sumOutputs = 0;
        for (Transaction.Input in : tx.getInputs()) {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            if (!uPool.contains(utxo) || !isValidTx(tx)) continue;
            Transaction.Output txOutput = uPool.getTxOutput(utxo);
            sumInputs += txOutput.value;
        }
        for (Transaction.Output out : tx.getOutputs()) {
            sumOutputs += out.value;
        }
        return sumInputs - sumOutputs;
    }
}
