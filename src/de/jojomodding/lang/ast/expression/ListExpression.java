package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.AtomValue;
import de.jojomodding.lang.value.ConstructorValue;
import de.jojomodding.lang.value.TupleValue;
import de.jojomodding.lang.value.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class ListExpression extends Expression {

    private List<Expression> subexprs;

    public ListExpression(Expression... exprs){
        this.subexprs = Arrays.asList(exprs);
        asChildren(exprs);
    }

    public ListExpression(List<Expression> exprs){
        subexprs = new ArrayList<>(exprs);
        asChildren(subexprs.toArray(new Expression[0]));
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Type bt = env.newType();
        for(Expression e : subexprs)
            env.forcetype(this, bt, e.elaborate(env));
        return new Datatype(env.getDatatypeDef("list"), bt);
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        Value result = new AtomValue(env.getDatatypeDef("list"), "nil");
        Datatype.DatatypeDef.DatatypeConstr listConstr = env.isDatatypeConstr("op::").get();
        ListIterator<Expression> le = subexprs.listIterator(subexprs.size());
        while (le.hasPrevious()){
            Expression e = le.previous();
            Value lv = e.evaluate(env);
            result = new ConstructorValue(listConstr).apply(new TupleValue(lv, result));
        }
        return result;
    }
}
