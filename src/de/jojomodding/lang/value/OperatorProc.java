package de.jojomodding.lang.value;

import de.jojomodding.lang.ast.expression.OperatorExpression;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.TupleType;

import java.util.Objects;

public class OperatorProc extends Value{

    private OperatorExpression.BinaryOperator opb;
    private OperatorExpression.UnaryOperator opu;

    public OperatorProc(OperatorExpression.BinaryOperator opb){
        this.opb = opb;
        this.opu = null;
    }

    public OperatorProc(OperatorExpression.UnaryOperator opu){
        this.opb = null;
        this.opu = opu;
    }

    public OperatorExpression.BinaryOperator getBinaryOperator(){
        return opb;
    }

    public OperatorExpression.UnaryOperator getUnaryOperator(){
        return opu;
    }

    public boolean isBinaryOperator(){
        return opb != null;
    }

    public boolean isUnaryOperator(){
        return opu != null;
    }

    @Override
    public String toString() {
        return "op"+(opb == null ? Objects.toString(opu) : opb.toString());
    }
}
