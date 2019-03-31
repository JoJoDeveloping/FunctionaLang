package de.jojomodding.lang.parsing;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SourceCode {

    private List<String> lines;
    private StringBuilder clb;
    private BufferedReader is;
    private CodePosition position;
    private int currentChar = -2;

    public SourceCode(InputStream source) {
        this.is = new BufferedReader(new InputStreamReader(source));
        this.clb = new StringBuilder();
        this.lines = new ArrayList<>();
        this.position = new CodePosition();
    }

    public char read() throws IOException {
        char c = peek();
        if(position.advance(c)){
            lines.add(clb.toString());
            clb = new StringBuilder();
        }else{
            clb.append(c);
        }
        currentChar = -2;
        return c;
    }

    public char peek() throws IOException{
        if(currentChar == -2){
            currentChar = is.read();
        }
        if(currentChar == -1)
            throw new EOFException();
        return (char) currentChar;
    }

    public boolean isAtEOF(){
        return currentChar == -1;
    }

    public List<String> lines() {
        return lines;
    }

    public CodePosition position() {
        return position;
    }
}
