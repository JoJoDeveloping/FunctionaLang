package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.TupleType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.TupleValue;
import de.jojomodding.lang.value.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TupleExpression extends Expression {

    private List<Expression> subexprs;

    public TupleExpression(Expression... exprs){
        this.subexprs = Arrays.asList(exprs);
        asChildren(exprs);
    }

    public TupleExpression(List<Expression> exprs){
        subexprs = new ArrayList<>(exprs);
        asChildren(subexprs.toArray(new Expression[0]));
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Type[] t = new Type[subexprs.size()];
        for(int i = 0 ; i < t.length; i++){
            t[i] = subexprs.get(i).elaborate(env);
        }
        return new TupleType(t);
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        Value[] t = new Value[subexprs.size()];
        for(int i = 0 ; i < t.length; i++){
            t[i] = subexprs.get(i).evaluate(env);
        }
        return new TupleValue(t);
    }
}
