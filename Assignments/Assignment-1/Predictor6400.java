import java.lang.*;

public class Predictor6400 extends Predictor {

    static final int m = 6, mOffset = 3, k = 4, n = 11, l = 3;
    static final int nMask = (1 << n) - 1, mMask = (1 << m) - 1;
    Table _GHR, _PHT;

    static private int mangle(long big, int small) {
        return (int) big ^ small;
    }

    public Predictor6400() {
        _GHR = new Table(1 << m, k);
        _PHT = new Table(1 << Math.max(n, k), l);
    }

    public void Train(long address, boolean outcome, boolean predict) {
        int ghrIdx = (int) ((address >> mOffset) & mMask);
        int phtIdx = mangle(address & nMask, _GHR.getInteger(ghrIdx, 0, k - 1));
        int value = _PHT.getInteger(phtIdx, 0, l - 1);
        if (outcome)
            _PHT.setInteger(phtIdx, 0, l - 1, Math.min(value + 1, (1 << l) - 1));
        else
            _PHT.setInteger(phtIdx, 0, l - 1, Math.max(value - 1, 0));
        _GHR.setInteger(ghrIdx, 0, k - 2, _GHR.getInteger(ghrIdx, 1, k - 1));
        _GHR.setBit(ghrIdx, k - 1, outcome);
    }


    public boolean predict(long address) {
        int ghrIdx = (int) ((address >> mOffset) & mMask);
        return _PHT.getBit(mangle(address & nMask, _GHR.getInteger(ghrIdx, 0, k - 1)), 0);
    }

}
