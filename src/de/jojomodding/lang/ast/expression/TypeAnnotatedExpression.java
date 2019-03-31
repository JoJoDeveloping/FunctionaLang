package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

public class TypeAnnotatedExpression extends Expression{

    private Expression e;
    private Type ft;

    public TypeAnnotatedExpression(Expression c, Type t){
        this.e = c;
        this.ft = t;
        asChildren(e);
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Type t = e.elaborate(env);
        env.forcetype(this, t, ft);
        return ft;
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        return e.evaluate(env);
    }
}
