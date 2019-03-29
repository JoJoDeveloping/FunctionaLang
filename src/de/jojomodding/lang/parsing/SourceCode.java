package de.jojomodding.lang.parsing;

import java.io.EOFException;
import java.util.Arrays;
import java.util.List;

public class SourceCode {

    private String source;
    private List<String> lines;
    private CodePosition position;

    public SourceCode(String code) {
        this.source = code;
        lines = Arrays.asList(source.split("\n"));
        this.position = new CodePosition();
    }

    public char read() throws EOFException {
        return position.popChar(source);
    }

    public String read(int n) throws EOFException {
        return position.popString(source, n);
    }

    public void seek(int n) throws EOFException {
        position.popString(source, n);
    }

    public String peek(int n) throws EOFException {
        return position.peekString(source, n);
    }

    public char peek() throws EOFException {
        return position.peek(source);
    }

    public int length(){
        return source.length();
    }

    public CodePosition position(){
        return position;
    }

    public boolean atEnd() {
        return position.isAtEOF(source);
    }

    public boolean startsWith(String s) {
        try {
            return position.peekString(source, s.length()).equals(s);
        } catch (EOFException e) {
            return false;
        }
    }

    public CodePosition positionAt(int i) throws EOFException {
        if(i==0)
            return position;
        CodePosition copy = position.copy();
        if(i>0) copy.popString(source, i);
        else copy.advanceBackwards(-i);
        return copy;
    }

    public List<String> lines(){
        return lines;
    }
}
