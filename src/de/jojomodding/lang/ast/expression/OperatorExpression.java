package de.jojomodding.lang.ast.expression;

import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ConstantValue;
import de.jojomodding.lang.value.ConstructorValue;
import de.jojomodding.lang.value.TupleValue;
import de.jojomodding.lang.value.Value;

import java.util.function.Supplier;

import static de.jojomodding.lang.type.BaseType.*;

public class OperatorExpression extends Expression{

    public enum BinaryOperator{
        ADD, SUB, MUL, LESS, GREATER, LEQ, GEQ, EQUAL, UNEQUAL, AND, OR, XOR, CONS;
    }

    public enum UnaryOperator{
        NEG, NOT
    }

    private Expression e1, e2;
    private BinaryOperator opb;
    private UnaryOperator opu;

    public OperatorExpression(Expression e1, BinaryOperator op, Expression e2){
        this.e1 = e1;
        this.e2 = e2;
        this.opu = null;
        this.opb = op;
        asChildren(e1, e2);
    }

    public OperatorExpression(UnaryOperator op, Expression e){
        this.e1 = e;
        this.e2 = null;
        this.opu = op;
        this.opb = null;
        asChildren(e1);
    }

    public static Type elaborate(ASTElement ast, BinaryOperator opb, Type t1, Type t2, ElabEnvironment env) throws ElaborationException{
        switch (opb){
            case ADD:
            case SUB:
            case MUL:
                env.forcetype(ast, t1, INT);
                env.forcetype(ast, t2, INT);
                return INT;
            case LESS:
            case GREATER:
            case LEQ:
            case GEQ:
                env.forcetype(ast, t1, INT);
                env.forcetype(ast, t2, INT);
                return BOOL;
            case EQUAL:
            case UNEQUAL:
                env.forcetype(ast, t1, t2);
                env.forceEquality(ast, t1);
                env.forceEquality(ast, t2);
                return BOOL;
            case AND:
            case OR:
            case XOR:
                env.forcetype(ast, t1, BOOL);
                env.forcetype(ast, t2, BOOL);
                return BOOL;
            case CONS:
                Datatype.DatatypeDef listDef = env.getDatatypeDef("list");
                Datatype lt = new Datatype(listDef, t1);
                env.forcetype(ast, t2, lt);
                return lt;
//                case APPEND:
//                    t = env.newType();
//                    listDef = env.getDatatypeDef("list");
//                    lt = new Datatype(listDef, t);
//                    env.forcetype(this, t1, lt);
//                    env.forcetype(this, t2, lt);
//                    return lt;
        }
        return null;
    }

    public static Type elaborate(ASTElement ast, UnaryOperator opu, Type t, ElabEnvironment env) throws ElaborationException {
        switch (opu){
            case NOT:
                env.forcetype(ast, t, BOOL);
                return BOOL;
            case NEG:
                env.forcetype(ast, t, INT);
                return INT;
        }
        return null;
    }


    @Override
    public Type elaborate(ElabEnvironment env) throws ElaborationException {
        if(opb != null){
            return elaborate(this, opb, e1.elaborate(env), e2.elaborate(env), env);
        }else if(opu != null){
            return elaborate(this, opu, e1.elaborate(env), env);
        }else throw new NullPointerException("opu and opb");
    }

    public static Value evaluate(ASTElement ele, BinaryOperator opb, Supplier_withException<Value, EvaluationException> v1s, Supplier_withException<Value, EvaluationException> v2s, Environment<Value> env) throws EvaluationException {
        switch (opb){
            case ADD: {
                ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) v1s.get(), v2 = (ConstantValue.IntegerValue) v2s.get();
                return new ConstantValue.IntegerValue(v1.getValue().add(v2.getValue())); }
            case SUB: {
                ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) v1s.get(), v2 = (ConstantValue.IntegerValue) v2s.get();
                return new ConstantValue.IntegerValue(v1.getValue().subtract(v2.getValue())); }
            case MUL: {
                ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) v1s.get(), v2 = (ConstantValue.IntegerValue) v2s.get();
                return new ConstantValue.IntegerValue(v1.getValue().multiply(v2.getValue())); }
            case LESS: {
                ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) v1s.get(), v2 = (ConstantValue.IntegerValue) v2s.get();
                return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) < 0); }
            case GREATER: {
                ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) v1s.get(), v2 = (ConstantValue.IntegerValue) v2s.get();
                return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) > 0); }
            case LEQ: {
                ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) v1s.get(), v2 = (ConstantValue.IntegerValue) v2s.get();
                return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) <= 0); }
            case GEQ: {
                ConstantValue.IntegerValue v1 = (ConstantValue.IntegerValue) v1s.get(), v2 = (ConstantValue.IntegerValue) v2s.get();
                return new ConstantValue.BooleanValue(v1.getValue().compareTo(v2.getValue()) >= 0); }
            case EQUAL:
                return new ConstantValue.BooleanValue(v1s.get().equals(v2s.get()));
            case UNEQUAL:
                return new ConstantValue.BooleanValue(!v1s.get().equals(v2s.get()));
            case AND:
                return new ConstantValue.BooleanValue(
                        ((ConstantValue.BooleanValue) v1s.get()).getValue()
                                && ((ConstantValue.BooleanValue) v2s.get()).getValue());
            case OR:
                return new ConstantValue.BooleanValue(
                        ((ConstantValue.BooleanValue) v1s.get()).getValue()
                                || ((ConstantValue.BooleanValue) v2s.get()).getValue());
            case XOR:
                return new ConstantValue.BooleanValue(
                        ((ConstantValue.BooleanValue) v1s.get()).getValue()
                                ^ ((ConstantValue.BooleanValue) v2s.get()).getValue());
            case CONS:
                ConstructorValue cv = (ConstructorValue) env.get("op::");
                return cv.apply(new TupleValue(v1s.get(), v2s.get()));
        }
        return null;
    }

    public static Value evaluate(ASTElement ele, UnaryOperator opu, Supplier_withException<Value, EvaluationException> v1s, Environment<Value> env) throws EvaluationException {
        switch (opu){
            case NOT:
                ConstantValue.BooleanValue vb = (ConstantValue.BooleanValue) v1s.get();
                return new ConstantValue.BooleanValue(!vb.getValue());
            case NEG:
                ConstantValue.IntegerValue vi = (ConstantValue.IntegerValue) v1s.get();
                return new ConstantValue.IntegerValue(vi.getValue().negate());
        }
        return null;
    }

    @Override
    public Value evaluate(Environment<Value> env) throws EvaluationException {
        if(opb != null){
            return evaluate(this, opb, () -> e1.evaluate(env), () -> e2.evaluate(env), env);
        }else if(opu != null){
            return evaluate(this, opu, () -> e1.evaluate(env), env);
        }else throw new NullPointerException("opu and opb");
    }

    public interface Supplier_withException <T,E extends Throwable> {
        T get() throws E;
    }

}
