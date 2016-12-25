package eakimov.netsort.settings;

import java.util.Scanner;

public class RunSettings {
    private final int archId;
    private final int x;
    private final boolean nSelected, mSelected, dSelected;
    private final int nStart, nEnd, nStep;
    private final int mStart, mEnd, mStep;
    private final int dStart, dEnd, dStep;

    public RunSettings(int archId, boolean nSelected, boolean mSelected, boolean dSelected, int x, int nStart, int nEnd, int nStep, int mStart, int mEnd, int mStep, int dStart, int dEnd, int dStep) {
        this.archId = archId;
        this.nSelected = nSelected;
        this.mSelected = mSelected;
        this.dSelected = dSelected;
        this.x = x;
        this.nStart = nStart;
        this.nEnd = nSelected ? nEnd : nStart;
        this.nStep = nStep;
        this.mStart = mStart;
        this.mEnd = mSelected ? mEnd : mStart;
        this.mStep = mStep;
        this.dStart = dStart;
        this.dEnd = dSelected ? dEnd : dStart;
        this.dStep = dStep;
    }

    public int getArchId() {
        return archId;
    }

    public int getX() {
        return x;
    }

    public int getnStart() {
        return nStart;
    }

    public int getnEnd() {
        return nEnd;
    }

    public int getnStep() {
        return nStep;
    }

    public int getmStart() {
        return mStart;
    }

    public int getmEnd() {
        return mEnd;
    }

    public int getmStep() {
        return mStep;
    }

    public int getdStart() {
        return dStart;
    }

    public int getdEnd() {
        return dEnd;
    }

    public int getdStep() {
        return dStep;
    }

    public boolean isnSelected() {
        return nSelected;
    }

    public boolean ismSelected() {
        return mSelected;
    }

    public boolean isdSelected() {
        return dSelected;
    }

    public int getProgressStart() {
        if (nSelected) {
            return nStart;
        }
        if (mSelected) {
            return mStart;
        }
        if (dSelected) {
            return dStart;
        }
        return 0;
    }

    public int getProgressEnd() {
        if (nSelected) {
            return nEnd;
        }
        if (mSelected) {
            return mEnd;
        }
        if (dSelected) {
            return dEnd;
        }
        return 0;
    }

    public int getProgressValue(int n, int m, int d) {
        if (nSelected) {
            return n;
        }
        if (mSelected) {
            return m;
        }
        if (dSelected) {
            return d;
        }
        return 0;
    }

    public int getSelectedValue(RunArguments arguments) {
        if (nSelected) {
            return arguments.getN();
        }
        if (mSelected) {
            return arguments.getM();
        }
        if (dSelected) {
            return arguments.getDelta();
        }
        return 0;
    }

    public String getSelectedName() {
        if (nSelected) {
            return "N";
        }
        if (mSelected) {
            return "M";
        }
        if (dSelected) {
            return "D";
        }
        return "???";
    }

    @Override
    public String toString() {
        String value = String.format("arch: %d\nx: %d", archId, x);
        value += "\nn: " + (nSelected ? String.format("from %d to %d step %d", nStart, nEnd, nStep) : nStart);
        value += "\nm: " + (mSelected ? String.format("from %d to %d step %d", mStart, mEnd, mStep) : mStart);
        value += "\nd: " + (dSelected ? String.format("from %d to %d step %d", dStart, dEnd, dStep) : dStart);
        value += "\n";
        return value;
    }

    public static RunSettings parse(String s) {
        Scanner v = new Scanner(s);
        return new RunSettings(
                v.nextInt(),
                v.nextInt() == 1,
                v.nextInt() == 1,
                v.nextInt() == 1,
                v.nextInt(),
                v.nextInt(),
                v.nextInt(),
                v.nextInt(),
                v.nextInt(),
                v.nextInt(),
                v.nextInt(),
                v.nextInt(),
                v.nextInt(),
                v.nextInt()
        );
    }
}
