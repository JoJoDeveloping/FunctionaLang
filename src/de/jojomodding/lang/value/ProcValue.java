package de.jojomodding.lang.value;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.pattern.PatternRow;

public class ProcValue extends Value {

    private PatternRow pr;
    private Environment<Value> env;

    public ProcValue(PatternRow pr, Environment<Value> env) {
        this.pr = pr;
        this.env = env;
    }

    public Environment<Value> getEvaluationEnvironment(){
        return env;
    }

    public PatternRow getBody(){
        return pr;
    }

    @Override
    public String toString() {
        return "fn";
    }
}
