package de.jojomodding.lang.parsing;

import de.jojomodding.lang.exception.LexerException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Lexer extends Thread{

    private SourceCode code;
    private Throwable ce;
    private Consumer<Token> tokenConsumer;

    public Lexer(InputStream s, Consumer<Token> tokenConsumer){
        code = new SourceCode(s);
        this.tokenConsumer = tokenConsumer;
    }

    private void add(Token.Basic token, CodePosition cp){
        addToken(token.at(cp));
    }

    private void addToken(Token k){
        tokenConsumer.accept(k);
    }

    public void lex() throws LexerException {
        try {
            while (true){
                CodePosition cp = code.position();
                if(code.isAtEOF()) break;
                char p1 = code.read();
                switch (p1){
                    case ' ': continue;
                    case '\t': continue;
                    case '\n': continue;
                    case '-':
                        if(code.peek()=='>'){
                            code.read();
                            add(Token.Basic.ARROW, cp);
                        }else add(Token.Basic.MINUS, cp);
                        continue;
                    case '(':
                        add(Token.Basic.LPAR, cp);
                        continue;
                    case ')':
                        add(Token.Basic.RPAR, cp);
                        continue;
                    case '[':
                        add(Token.Basic.CLPAR, cp);
                        continue;
                    case ']':
                        add(Token.Basic.CRPAR, cp);
                        continue;
                    case '_':
                        add(Token.Basic.WILDCARD, cp);
                        continue;
                    case ':':
                        if(code.peek() == ':'){
                            code.read();
                            add(Token.Basic.CONS, cp);
                        }else add(Token.Basic.COLON, cp);
                        continue;
                    case '=':
                        char p2 = code.peek();
                        switch (p2){
                            case '=':
                                code.read();
                                add(Token.Basic.EQUAL, cp);
                                continue;
                            case '>':
                                code.read();
                                add(Token.Basic.BIGARROW, cp);
                                continue;
                            default:
                                add(Token.Basic.DEFEQUAL, cp);
                                continue;
                        }
                    case '!':
                        if(code.peek()=='='){
                            code.read();
                            add(Token.Basic.UNEQUAL, cp);
                        }else add(Token.Basic.NOT, cp);
                        continue;
                    case '<':
                        if(code.peek()=='='){
                            code.read();
                            add(Token.Basic.LESSEQUAL, cp);
                        }else add(Token.Basic.LESS, cp);
                        continue;
                    case '>':
                        if(code.peek()=='='){
                            code.read();
                            add(Token.Basic.GREATEREQUAL, cp);
                        }else add(Token.Basic.GREATER, cp);
                        continue;
                    case '|':
                        if(code.peek()=='|'){
                            code.read();
                            add(Token.Basic.OR, cp);
                            continue;
                        }
                        add(Token.Basic.VBAR, cp);
                        continue;
                    case '&':
                        if(code.peek()=='&'){
                            code.read();
                            add(Token.Basic.OR, cp);
                            continue;
                        }
                        throw new LexerException(cp, "Unknown token "+p1+code.peek());
                    case '+':
                        add(Token.Basic.PLUS, cp);
                        continue;
                    case '*':
                        add(Token.Basic.STAR, cp);
                        continue;
                    case ',':
                        add(Token.Basic.COMMA, cp);
                        continue;
                    case ';':
                        add(Token.Basic.SEMICOLON, cp);
                        continue;
                    case '\'':
                        char cc = code.read();
                        if(cc=='\'') this.addToken(new Token.IDToken(lexID(code.read()), cp, Token.Basic.TYPE_TVAR_EQ));
                        else this.addToken(new Token.IDToken(lexID(cc), cp, Token.Basic.TYPE_TVAR));
                        continue;
                    case '#':
                        BigInteger i = lexNum(code.read());
                        if(i.compareTo(BigInteger.ONE) < 0) throw new LexerException(cp, "Projection of nonpositive tuple entry");
                        this.addToken(new Token.IntegerToken(i, cp, Token.Basic.TYPE_PROJ));
                        continue;
                    default:
                        if(isIDStart(p1)){
                            String id = lexID(p1);
                            switch (id){
                                case "int":
                                    add(Token.Basic.INT, cp);
                                    continue;
                                case "bool":
                                    add(Token.Basic.BOOL, cp);
                                    continue;
                                case "fn":
                                    add(Token.Basic.FN, cp);
                                    continue;
                                case "if":
                                    add(Token.Basic.IF, cp);
                                    continue;
                                case "then":
                                    add(Token.Basic.THEN, cp);
                                    continue;
                                case "else":
                                    add(Token.Basic.ELSE, cp);
                                    continue;
                                case "true":
                                    add(Token.Basic.TRUE, cp);
                                    continue;
                                case "false":
                                    add(Token.Basic.FALSE, cp);
                                    continue;
                                case "fun":
                                    add(Token.Basic.FUN, cp);
                                    continue;
                                case "val":
                                    add(Token.Basic.VAL, cp);
                                    continue;
                                case "datatype":
                                    add(Token.Basic.DATATYPE, cp);
                                    continue;
                                case "of":
                                    add(Token.Basic.OF, cp);
                                    continue;
                                case "as":
                                    add(Token.Basic.AS, cp);
                                    continue;
                            }
                            this.addToken(new Token.IDToken(id, cp));
                            continue;
                        }else if(Character.isDigit(p1)){
                            this.addToken(new Token.IntegerToken(lexNum(p1), cp));
                            continue;
                        }
                        throw new LexerException(cp, "Unknown char "+p1);
                }
            }
        }catch (IOException e){
            throw new LexerException(null, "Unexpected error: "+e.getMessage());
        }
    }

    private String lexID(char start) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        while(!code.isAtEOF() && isID(code.peek())){
            sb.append(code.read());
        }
        return sb.toString();
    }

    private BigInteger lexNum(char start) throws IOException {
        BigInteger v = BigInteger.ZERO;
        int base = 10;
        if(start=='0'){
            char s = code.peek();
            switch(s){
                case 'b':
                case 'B':
                    base=2;
                    code.read();
                    break;
                case 'X':
                case 'x':
                    base = 16;
                    code.read();
                    break;
                default:
                    if(s <= '7' && s > '0')
                        base=8;
                    else return BigInteger.ZERO;
            }
        }else v = BigInteger.valueOf(toNum(start, base));
        while(!code.isAtEOF()){
            int i = toNum(code.peek(), base);
            if(i<0) break;
            code.read();
            v=v.multiply(BigInteger.valueOf(base)).add(BigInteger.valueOf(i));
        }
        return v;
    }

    private boolean isIDStart(char x){
        return x=='_' || Character.isAlphabetic(x);
    }

    private boolean isID(char x){
        return isIDStart(x) || Character.isDigit(x) || x=='\'';
    }

    private int toNum(char x, int base){
        int r = -1;
        if(x >= '0' && x <= '9')
            r=x-'0';
        if(x >= 'A' && x <= 'Z')
            r=x-'A';
        if(x >= 'a' && x <= 'z')
            r=x-'a';
        if(r >= base) return -1;
        return r;
    }

    public List<String> lines() {
        return code.lines();
    }
}
