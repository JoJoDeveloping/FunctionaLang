package de.jojomodding.lang.exception;

import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.parsing.CodePosition;

public class EvaluationException extends LangException {

    private ASTElement source;

    private String reason;

    public EvaluationException(ASTElement me, String reason){
        this.source = me;
        this.reason = reason;
    }


    @Override
    public CodePosition position() {
        return source.position();
    }

    public String format() {
        if(source.position() != null)
            return "Exception during evaluation at "+source.position()+": "+reason;
        else return "Exception during evaluation: "+reason;
    }

}
