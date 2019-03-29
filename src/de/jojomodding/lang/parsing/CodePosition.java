package de.jojomodding.lang.parsing;

import java.io.EOFException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CodePosition {

    private int line, charAt, actualChar;
    private Map<Integer, Integer> lineLength;

    public CodePosition(){
        this.line = 0;
        this.charAt = 0;
        this.actualChar = 0;
        lineLength = new HashMap<>();
    }

    public CodePosition(CodePosition p) {
        this.line = p.line;
        this.charAt = p.charAt;
        this.actualChar = p.actualChar;
        this.lineLength = p.lineLength;
    }

    public void advance(char... data){
        for(char c : data){
            actualChar++;
            if(c=='\n'){
                lineLength.put(line, charAt);
                line++;
                charAt=0;
            }else charAt++;
        }
    }

    public char popChar(String sc) throws EOFException {
        char c = peek(sc);
        advance(c);
        return c;
    }

    public String popString(String sc, int len) throws EOFException {
        String s = peekString(sc, len);
        advance(s.toCharArray());
        return s;
    }

    public boolean isAtEOF(String s){
        return s.length() <= actualChar;
    }

    public int line(){
        return line;
    }

    public int posInLine(){
        return charAt;
    }

    public String peekString(String sc, int len) throws EOFException {
        try{
            return sc.substring(actualChar, len);
        }catch (StringIndexOutOfBoundsException e){
            throw new EOFException();
        }
    }

    public char peek(String s) throws EOFException{
        try {
            return s.charAt(actualChar);
        }catch (StringIndexOutOfBoundsException e){
            throw new EOFException();
        }
    }

    public CodePosition copy() {
        CodePosition cp = new CodePosition();
        cp.charAt = charAt;
        cp.line = line;
        cp.actualChar = actualChar;
        cp.lineLength = new HashMap<>(lineLength);
        return cp;
    }

    public void advanceBackwards(int amnt){
        for(;amnt>0&&actualChar>0; amnt--){
            actualChar--;
            if(charAt==0){
                line--;
                charAt=lineLength.get(line);
            }else charAt--;
        }
    }

    @Override
    public String toString() {
        return "line "+line+", char "+charAt;
    }
}
