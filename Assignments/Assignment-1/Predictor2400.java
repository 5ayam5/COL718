import java.lang.*;

public class Predictor2400 extends Predictor {

    static final int m = 5, mOffset = 3, k = 5, n = 10, pht_bits = 2,
                 g = 4, gpt_bits = 3, c = 5, cpt_bits = 3;
    Table _GHR, _PHT, _GPT, _CPT;
    Register _globalHistory;

    static private int mangle(long big, long small, int mask) {
        return (int) (big ^ small) & mask;
    }

    public Predictor2400() {
        _GHR = new Table(1 << m, k);
        _PHT = new Table(1 << Math.max(n, k), pht_bits);
        for (int i = 0; i < _PHT.getNumEntries(); i++)
            _PHT.setBit(i, 0, true);
        _GPT = new Table(1 << g, gpt_bits);
        _CPT = new Table(1 << c, cpt_bits);
        for (int i = 0; i < _CPT.getNumEntries(); i++)
            _CPT.setBit(i, 0, true);
        _globalHistory = new Register(g);
    }

    public void Train(long address, boolean outcome, boolean predict) {
        int globalHistory = _globalHistory.getInteger(0, g - 1);

        int cptIdx = mangle(address, globalHistory, _CPT.getNumEntries() - 1);
        int cptValue = _CPT.getInteger(cptIdx, 0, cpt_bits - 1);
        boolean local = predictLocal(address), global = predictGlobal(address);
        if (local != global)
            if (local == outcome)
                _CPT.setInteger(cptIdx, 0, cpt_bits - 1, Math.min(cptValue + 1, (1 << cpt_bits) - 1));
            else
                _CPT.setInteger(cptIdx, 0, cpt_bits - 1, Math.max(cptValue - 1, 0));

        int ghrIdx = (int) ((address >> mOffset) & ((1 << m) - 1));
        int phtIdx = mangle(address, _GHR.getInteger(ghrIdx, 0, k - 1),
                _PHT.getNumEntries() - 1);
        int phtValue = _PHT.getInteger(phtIdx, 0, pht_bits - 1);
        if (outcome)
            _PHT.setInteger(phtIdx, 0, pht_bits - 1, Math.min(phtValue + 1, (1 << pht_bits) - 1));
        else
            _PHT.setInteger(phtIdx, 0, pht_bits - 1, Math.max(phtValue - 1, 0));

        int gptValue = _GPT.getInteger(globalHistory, 0, gpt_bits - 1);
        if (outcome)
            _GPT.setInteger(globalHistory, 0, gpt_bits - 1, Math.min(gptValue + 1, (1 << gpt_bits) - 1));
        else
            _GPT.setInteger(globalHistory, 0, gpt_bits - 1, Math.max(gptValue - 1, 0));

        _GHR.setInteger(ghrIdx, 0, k - 2, _GHR.getInteger(ghrIdx, 1, k - 1));
        _GHR.setBit(ghrIdx, k - 1, outcome);

        _globalHistory.setInteger(0, g - 2, (globalHistory << 1) & ((1 << g) - 1));
        _globalHistory.setBitAtIndex(g - 1, outcome);
    }

    private boolean predictLocal(long address) {
        return _PHT.getBit(mangle(address,
                    _GHR.getInteger((int) ((address >> mOffset) & ((1 << m) - 1)), 0, k - 1),
                    _PHT.getNumEntries() - 1), 0);
    }

    private boolean predictGlobal(long address) {
        int globalHistory = _globalHistory.getInteger(0, g - 1);
        return _GPT.getBit(globalHistory, 0);
    }

    public boolean predict(long address) {
        if (_CPT.getBit(mangle(address, _globalHistory.getInteger(0, g - 1),
                        _CPT.getNumEntries() - 1), 0))
            return predictLocal(address);
        return predictGlobal(address);
    }

}
