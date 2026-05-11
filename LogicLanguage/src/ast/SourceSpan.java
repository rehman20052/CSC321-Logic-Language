package ast;

public class SourceSpan {
    public final int start;
    public final int end;

    public SourceSpan(int start, int end) {
        this.start = start;
        this.end = end;
    }
}