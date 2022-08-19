import java.lang.*;

public class Predictor2400 extends Predictor {

    static final int n = 9, k = 4, m = 8;
    Table _IST;
    Table _lastPred;

    public Predictor2400() {
        _IST = new Table(1 << n, k);
        _lastPred = new Table(1 << m, 1);
    }

    public void Train(long address, boolean outcome, boolean predict) {
        int idx = (int) (address & ((1 << n) - 1));
        int satCount = _IST.getInteger(idx, 0, k - 1);
        if (outcome)
            _IST.setInteger(idx, 0, k - 1, Math.min(1 << k, satCount + 1));
        else
            _IST.setInteger(idx, 0, k - 1, Math.max(0, satCount - 1));
        _lastPred.setBit((int) (address & ((1 << m) - 1)), 0, outcome);
    }


    public boolean predict(long address) {
        int istVal = _IST.getInteger((int) (address & ((1 << n) - 1)), 0, k - 1);
        boolean last = _lastPred.getBit((int) (address & ((1 << m) - 1)), 0);
        final int rightMask = (1 << (k / 2)) - 1, leftMask = ((1 << k) - 1) - rightMask;
        if ((istVal & leftMask) == 0 || (istVal & leftMask) == leftMask)
            return istVal >= (1 << (k - 1));
        return last;
    }

}
