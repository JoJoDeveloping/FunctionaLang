package de.jojomodding.lang.parsing;

import java.math.BigInteger;

public class Token {

    public enum Basic{
        ARROW("->"), LPAR("("), RPAR(")"), INT("int"), BOOL("bool"),
        FUN("fun"), DEFEQUAL("="), VAL("val"), DATATYPE("datatype"), OF("of"),
        VBAR("|"), WILDCARD("_"), AS("as"), CONS("::"), CLPAR("["), CRPAR("]"),
        FN("fn"), COLON(":"), BIGARROW("=>"), IF("if"), THEN("then"), ELSE("else"),
        EQUAL("=="), UNEQUAL("!="), LESS("<"), GREATER(">"), LESSEQUAL("<="), GREATEREQUAL(">="),
        AND("&&"), OR("||"), XOR("^"), NOT("!"), PLUS("+"), MINUS("-"), TILDE("~"), STAR("*"), COMMA(","),
        TRUE("true"), FALSE("false"), SEMICOLON(";"), OP("op"),
        CASE("case"), LET("let"), IN("in"), END("end"),
        TYPE_INTEGER(null), TYPE_IDENT(null), TYPE_TVAR(null), TYPE_TVAR_EQ(null), TYPE_PROJ(null), EOF(null);

        private String rep;

        public Token at(CodePosition p){
            return new Token(this, p);
        }

        Basic(String s){
            this.rep = s;
        }

        @Override
        public String toString() {
            return rep;
        }
    }

    public static class IntegerToken extends Token{

        private BigInteger v;

        public IntegerToken(BigInteger value, CodePosition p){
            super(Basic.TYPE_INTEGER, p);
            this.v = value;
        }

        public IntegerToken(BigInteger value, CodePosition p, Basic type){
            super(type, p);
            this.v = value;
        }

        public BigInteger value(){
            return v;
        }

        @Override
        public String toString() {
            switch (rep()){
                case TYPE_INTEGER: return v.toString();
                case TYPE_PROJ: return "#"+v.toString();
                default: return v.toString();
            }
        }
    }

    public static class IDToken extends Token{
        private String id;

        public IDToken(String id, CodePosition p){
            super(Basic.TYPE_IDENT, p);
            this.id = id;
        }
        public IDToken(String id, CodePosition p, Basic type){
            super(type, p);
            this.id = id;
        }

        public String value(){
            return id;
        }

        @Override
        public String toString() {
            switch (rep()){
                case TYPE_IDENT: return id;
                case TYPE_TVAR:  return "'"+id;
                default: return id;
            }
        }
    }


    private CodePosition p;
    private Token.Basic rep;

    private Token(Token.Basic rep, CodePosition p){
        this.rep = rep;
        this.p = new CodePosition(p);
    }

    public CodePosition getPosition(){
        return p;
    }

    public boolean is(Token.Basic t){
        return rep==t;
    }

    @Override
    public String toString() {
        return rep.toString();
    }

    public Basic rep(){
        return rep;
    }
}
