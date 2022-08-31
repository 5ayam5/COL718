import java.lang.*;

public class Predictor6400 extends Predictor {

    static final int m = 8, mOffset = 3, k = 6, n = 10, l = 3, g = 8, gl = 3;
    static final int nMask = (1 << n) - 1, mMask = (1 << m) - 1, gMask = (1 << g) - 1;
    Table _GHR, _PHT, _GPT, _CPT;
    Register _globalHistory;

    static private int mangle(long big, int small, int mask) {
        return (int) (((big + small) * (big + small + 1)) / 2 + small) & mask;
    }

    public Predictor6400() {
        _GHR = new Table(1 << m, k);
        _PHT = new Table(1 << Math.max(n, k), l);
        _GPT = new Table(1 << g, l);
        _CPT = new Table(1 << g, gl);
        _globalHistory = new Register(g);
    }

    public void Train(long address, boolean outcome, boolean predict) {
        int globalHistory = _globalHistory.getInteger(0, g - 1);

        int cptIdx = mangle(address & gMask, globalHistory, gMask);
        int cptValue = _CPT.getInteger(cptIdx, 0, gl - 1);
        boolean local = predictLocal(address), global = predictGlobal(address);
        if (local != global) {
            if (local == outcome)
                _CPT.setInteger(cptIdx, 0, gl - 1, Math.min(cptValue + 1, (1 << gl) - 1));
            else
                _CPT.setInteger(cptIdx, 0, gl - 1, Math.max(cptValue - 1, 0));
        }

        int ghrIdx = (int) ((address >> mOffset) & mMask);
        int phtIdx = mangle(address & nMask, _GHR.getInteger(ghrIdx, 0, k - 1), nMask);
        int phtValue = _PHT.getInteger(phtIdx, 0, l - 1);
        if (outcome)
            _PHT.setInteger(phtIdx, 0, l - 1, Math.min(phtValue + 1, (1 << l) - 1));
        else
            _PHT.setInteger(phtIdx, 0, l - 1, Math.max(phtValue - 1, 0));

        int gptValue = _GPT.getInteger(globalHistory, 0, l - 1);
        if (outcome)
            _GPT.setInteger(globalHistory, 0, l - 1, Math.min(gptValue + 1, (1 << l) - 1));
        else
            _GPT.setInteger(globalHistory, 0, l - 1, Math.max(gptValue - 1, 0));

        _GHR.setInteger(ghrIdx, 0, k - 2, _GHR.getInteger(ghrIdx, 1, k - 1));
        _GHR.setBit(ghrIdx, k - 1, outcome);

        _globalHistory.setInteger(0, g - 2, (globalHistory << 1) & ((1 << g) - 1));
        _globalHistory.setBitAtIndex(g - 1, outcome);
    }

    private boolean predictLocal(long address) {
        return _PHT.getBit(mangle(address & nMask,
                    _GHR.getInteger((int) ((address >> mOffset) & mMask), 0, k - 1), nMask), 0);
    }

    private boolean predictGlobal(long address) {
        int globalHistory = _globalHistory.getInteger(0, g - 1);
        return _GPT.getBit(globalHistory, 0);
    }

    public boolean predict(long address) {
        if (_CPT.getBit(mangle(address & gMask, _globalHistory.getInteger(0, g - 1), gMask), 0))
            return predictLocal(address);
        return predictGlobal(address);
    }

}
