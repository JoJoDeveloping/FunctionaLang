package de.jojomodding.lang.exception;

import de.jojomodding.lang.parsing.CodePosition;

public class LexerException extends LangException {

    private CodePosition source;

    private String reason;

    public LexerException(CodePosition me, String reason){
        this.source = me;
        this.reason = reason;
    }

    @Override
    public CodePosition position() {
        return source;
    }

    public String format() {
        if(source != null)
            return "Exception during lexing at "+source.toString()+": "+reason;
        else return "Exception during lexing: "+reason;
    }

}
