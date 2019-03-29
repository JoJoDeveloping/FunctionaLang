package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.TupleType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.TupleValue;
import de.jojomodding.lang.value.Value;

public class ProjectionExpression extends Expression {

    private int index;
    private Expression expr;

    public ProjectionExpression(int index, Expression expr){
        if(index < 1) throw new RuntimeException("Invalid tuple size "+index);
        this.index = index;
        this.expr = expr;
        asChildren(expr);
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Type t = env.resolve(expr.elaborate(env));
        if(!(t instanceof TupleType)) throw new ElaborationException(this, "Cannot project something that is not a tuple!");
        if(((TupleType) t).entries().size() < index) throw new ElaborationException(this, "Cannot project "+index+"th element out of tuple of size "+((TupleType) t).entries());
        return ((TupleType) t).entries().get(index-1);
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        Value t = expr.evaluate(env);
        if(!(t instanceof TupleValue)) throw new EvaluationException(this, "Cannot project something that is not a tuple!");
        if(((TupleValue) t).entries().size() < index) throw new EvaluationException(this, "Cannot project "+index+"th element out of tuple of size "+((TupleValue) t).entries());
        return ((TupleValue) t).entries().get(index-1);
    }
}
