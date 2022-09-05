import java.lang.*;

public class Predictor32000 extends Predictor {

    static final int m = 2, mOffset = 12, k = 34, n = 7, weight_bits = 7, pht_bits = weight_bits * (k + 1);
    static final double threshold = ((1 << (weight_bits - 1)) * (k + 1)) * 0.0135;
    Table _PHT, _GHR;

    public Predictor32000() {
        _PHT = new Table(1 << n, pht_bits);
        for (int i = 0; i < _PHT.getNumEntries(); i++)
            for (int j = 0; j < k; j++)
                _PHT.setBit(i, j * weight_bits, true);

        _GHR = new Table(1 << m, k + 1);
        for (int i = 0; i < _GHR.getNumEntries(); i++)
            _GHR.setBit(i, k, true);
    }

    public void Train(long address, boolean outcome, boolean predict) {
        int y = perceptronOutput(address);
        int phtIndex = (int) address & ((1 << n) - 1);
        int ghrIndex = (int) (address >> mOffset) & ((1 << m) - 1);

        if (outcome != predict || Math.abs(y) < threshold) {
            for (int i = 0; i <= k; i++) {
                int weight = _PHT.getInteger(phtIndex, i * weight_bits, (i + 1) * weight_bits - 1);
                int xi = _GHR.getBit(ghrIndex, i) ? 1 : -1;
                if (outcome)
                    weight = Math.min(weight + xi, ((1 << weight_bits) - 1));
                else
                    weight = Math.max(weight - xi, 0);

                _PHT.setInteger(phtIndex, i * weight_bits, (i + 1) * weight_bits - 1, weight);
            }
        }

        for (int i = 0; i < k - 1; i++)
            _GHR.setBit(ghrIndex, i, _GHR.getBit(ghrIndex, i + 1));
        _GHR.setBit(ghrIndex, k - 1, outcome);
    }

    private int perceptronOutput(long address) {
        int y = 0;
        int phtIndex = (int) address & ((1 << n) - 1);
        int ghrIndex = (int) (address >> mOffset) & ((1 << m) - 1);
        for (int i = 0; i <= k; i++) {
            int weight = _PHT.getInteger(phtIndex, i * weight_bits, (i + 1) * weight_bits - 1) - (1 << (weight_bits - 1));
            int xi = _GHR.getBit(ghrIndex, i) ? 1 : -1;
            y += weight * xi;
        }
        return y;
    }

    public boolean predict(long address) {
        return perceptronOutput(address) > 0;
    }

}
