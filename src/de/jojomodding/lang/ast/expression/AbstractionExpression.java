package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.pattern.PatternRow;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.FunctionType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ProcValue;
import de.jojomodding.lang.value.Value;

import java.util.Map;

public class AbstractionExpression extends Expression {

    private PatternRow pr;

    public AbstractionExpression(PatternRow pr) {
        this.pr = pr;
        asChildren(pr);
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        Map.Entry<Type, Type> ent = pr.elaborate(env);
        return new FunctionType(ent.getKey(), ent.getValue());
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        return new ProcValue(pr, env);
    }
}
