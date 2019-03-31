package de.jojomodding.lang.parsing;

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

    public boolean advance(char c){
        actualChar++;
        if(c=='\n'){
            lineLength.put(line, charAt);
            line++;
            charAt=0;
        }else charAt++;
        return charAt == 0;
    }

    @Override
    public String toString() {
        return "line "+line+", char "+charAt;
    }

    public int actualChar() {
        return actualChar;
    }

    public int line(){
        return line;
    }

    public int charInLine(){
        return charAt;
    }
}
