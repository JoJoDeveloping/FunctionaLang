package de.jojomodding.lang.ast.def;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.value.Value;

import java.util.List;

public abstract class Definition extends ASTElement{

    @Override
    public Definition at(CodePosition cp){
        return (Definition) super.at(cp);
    }

    public abstract List<String> elaborate(ElabEnvironment env) throws ElaborationException;

    public abstract void evaluate(Environment<Value> env) throws EvaluationException;
}
