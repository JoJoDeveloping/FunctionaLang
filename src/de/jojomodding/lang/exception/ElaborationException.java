package de.jojomodding.lang.exception;

import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.parsing.CodePosition;

public class ElaborationException extends LangException {

    private ASTElement source;

    private String reason;

    public ElaborationException(ASTElement me, String reason){
        this.source = me;
        this.reason = reason;
    }


    @Override
    public CodePosition position() {
        return source.position();
    }

    public String format() {
        if(source.position() != null)
            return "Exception during elaboration at "+source.position()+": "+reason;
        else return "Exception during elaboration: "+reason;
    }


}
