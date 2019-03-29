package de.jojomodding.lang.exception;

import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.parsing.Token;

public class ParserException extends LangException {

    private Token source;

    private String reason;

    public ParserException(Token me, String reason){
        this.source = me;
        this.reason = reason;
    }

    @Override
    public CodePosition position() {
        if(source!=null) return source.getPosition();
        return null;
    }

    public String format() {
        if(source != null)
            return "Exception during parsing at "+source.getPosition().toString()+": "+reason+", got "+source;
        else return "Exception during parsing: "+reason;
    }

    public Token getToken() {
        return source;
    }
}
