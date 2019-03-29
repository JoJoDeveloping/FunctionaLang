package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

public abstract class Expression extends ASTElement {

    @Override
    public Expression at(CodePosition cp){
        return (Expression) super.at(cp);
    }

    public abstract Type elaborate(ElabEnvironment env) throws ElaborationException;

    public abstract Value evaluate(Environment<Value> env) throws EvaluationException;

}
