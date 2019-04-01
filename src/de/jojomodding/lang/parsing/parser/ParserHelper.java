package de.jojomodding.lang.parsing.parser;

import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ParserException;
import de.jojomodding.lang.parsing.Token;

public abstract class ParserHelper<T> {

    protected final Parser p;
    private final String name;
    protected final ElabEnvironment env;

    public ParserHelper(String name, Parser p){
        this.p = p;
        this.name = name;
        this.env = p.getElabEnviron();
    }


    protected Token.Basic current_rep() throws ParserException {
        return p.current_rep();
    }

    protected Token current() throws ParserException {
        return p.current();
    }

    protected Token next() throws ParserException {
        return p.next();
    }

    protected void advance(){
        p.advance();
    }

    public abstract T parse() throws ParserException;

}
