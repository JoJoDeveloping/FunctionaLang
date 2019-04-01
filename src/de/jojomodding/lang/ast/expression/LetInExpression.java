package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.ast.def.Definition;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.List;

public class LetInExpression extends Expression{

    private List<Definition> defs;
    private Expression e;

    public LetInExpression(List<Definition> d, Expression e){
        this.defs = d;
        this.e = e;
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        ElabEnvironment letenv = env.deepCopy();
        for(Definition d : defs)
            d.elaborate(letenv);
        return e.elaborate(letenv);
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        Environment<Value> letenv = env.deepCopy();
        for (Definition d : defs)
            d.evaluate(letenv);
        return e.evaluate(letenv);
    }
}
