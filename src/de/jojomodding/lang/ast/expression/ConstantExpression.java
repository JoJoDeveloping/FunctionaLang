package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ConstantValue;
import de.jojomodding.lang.value.Value;

public class ConstantExpression extends Expression {

    private ConstantValue value;

    public ConstantExpression(ConstantValue v){
        this.value = v;
    }

    @Override
    public Type elaborate(ElabEnvironment env) {
        return value.getType(env);
    }

    @Override
    public Value evaluate(Environment<Value> env) {
        return value;
    }
}
