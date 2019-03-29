package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.QuantizedType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

public class VariableExpression extends Expression {

    private String name;

    public VariableExpression(String varname){
        this.name = varname;
    }

    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        if(!env.has(name))
            throw new ElaborationException(this, "Variable "+name+" is undefined");
        Type t = env.get(name);
        if(t instanceof QuantizedType)
            return env.dequantizeType((QuantizedType) t);
        return t;
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        if(!env.has(name))
            throw new EvaluationException(this, "Variable "+name+" is undefined");
        return env.get(name);
    }
}
