package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ConstantValue;
import de.jojomodding.lang.value.Value;

import static de.jojomodding.lang.type.BaseType.*;

public class ConditionalExpression extends Expression {

    private Expression cond, thenC, elseC;

    public ConditionalExpression(Expression i, Expression t, Expression e){
        this.cond = i;
        this.thenC = t;
        this.elseC = e;
        asChildren(i, t, e);
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Type ift = cond.elaborate(env);
        env.forcetype(this, ift, BOOL);
        Type t1 = thenC.elaborate(env), t2=elseC.elaborate(env);
        env.forcetype(this, t1, t2);
        return t1;
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        ConstantValue.BooleanValue b = (ConstantValue.BooleanValue) cond.evaluate(env);
        if(b.getValue())
            return thenC.evaluate(env);
        return elseC.evaluate(env);
    }
}
