package eakimov.netsort.settings;

import java.io.Serializable;

public class RunArguments implements Serializable {
    private final int arch;
    private final int x;
    private final int n;
    private final int m;
    private final int delta;

    public RunArguments(int arch, int x, int n, int m, int delta) {
        this.arch = arch;
        this.x = x;
        this.n = n;
        this.m = m;
        this.delta = delta;
    }

    public int getArch() {
        return arch;
    }

    public int getX() {
        return x;
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

    public int getDelta() {
        return delta;
    }
}
