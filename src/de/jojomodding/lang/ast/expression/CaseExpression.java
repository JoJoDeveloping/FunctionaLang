package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.ast.pattern.PatternRow;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.Map;

public class CaseExpression extends Expression {

    private Expression cased;
    private PatternRow row;

    public CaseExpression(Expression e, PatternRow pr){
        this.cased = e;
        this.row = pr;
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Type st = cased.elaborate(env);
        Map.Entry<Type, Type> tt = row.elaborate(env);
        env.forcetype(this, tt.getValue(), st);
        return tt.getKey();
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        return row.evaluate(env, cased.evaluate(env));
    }
}
