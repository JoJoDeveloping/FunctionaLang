package de.jojomodding.lang.parsing.parser;

import de.jojomodding.lang.ast.expression.*;
import de.jojomodding.lang.ast.pattern.PatternRow;
import de.jojomodding.lang.exception.ParserException;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.parsing.Token;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.AtomValue;

import java.util.LinkedList;
import java.util.List;

import static de.jojomodding.lang.parsing.Token.Basic.*;

public class ExpressionParser extends ParserHelper<Expression> {

    public ExpressionParser(Parser p) {
        super("exp", p);
    }

    @Override
    public Expression parse() throws ParserException {
        return exp();
    }


    private Expression exp() throws ParserException {
        switch (current_rep()){
            case FN:
                CodePosition cp = current().getPosition();
                advance();
                PatternRow pr = this.p.patrow();
                return new AbstractionExpression(pr).at(cp);
            case IF:
                cp = current().getPosition();
                advance();
                Expression cond = exp();
                if(current_rep() == THEN){
                    advance();
                    Expression tc = exp();
                    if(current_rep() == ELSE){
                        advance();
                        Expression ec = exp();
                        return new ConditionalExpression(cond, tc, ec).at(cp);
                    }else throw new ParserException(current(), "expected \"else\"");
                }else throw new ParserException(current(), "expected \"then\"");
            default: return anexp();
        }
    }

    private Expression anexp() throws ParserException {
        Expression p = oexp_(cexp());
        switch (current_rep()){
            case COLON:
                advance();
                Type t = this.p.ty();
                return new TypeAnnotatedExpression(p, t).at(p.position());
            default: return p;
        }

    }

    private Expression oexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case OR:
                advance();
                return oexp_(new OperatorExpression(given, OperatorExpression.BinaryOperator.OR, exp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression cexp() throws ParserException {
        return cexp_(xexp());
    }


    private Expression cexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case AND:
                advance();
                return cexp_(new OperatorExpression(given, OperatorExpression.BinaryOperator.ADD, cexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression xexp() throws ParserException {
        return xexp_(eqexp());
    }


    private Expression xexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case XOR:
                advance();
                return xexp_(new OperatorExpression(given, OperatorExpression.BinaryOperator.XOR, xexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression eqexp() throws ParserException {
        Expression given = cmpexp();
        switch (current_rep()){
            case EQUAL:
                advance();
                return new OperatorExpression(given, OperatorExpression.BinaryOperator.EQUAL, cmpexp()).at(given.position());
            case UNEQUAL:
                advance();
                return new OperatorExpression(given, OperatorExpression.BinaryOperator.UNEQUAL, cmpexp()).at(given.position());
            default:
                return given;
        }
    }

    private Expression cmpexp() throws ParserException {
        Expression given = lexp();
        switch (current_rep()){
            case GREATER:
                advance();
                return new OperatorExpression(given, OperatorExpression.BinaryOperator.GREATER, lexp()).at(given.position());
            case LESS:
                advance();
                return new OperatorExpression(given, OperatorExpression.BinaryOperator.LESS, lexp()).at(given.position());
            case GREATEREQUAL:
                advance();
                return new OperatorExpression(given, OperatorExpression.BinaryOperator.GEQ, lexp()).at(given.position());
            case LESSEQUAL:
                advance();
                return new OperatorExpression(given, OperatorExpression.BinaryOperator.LEQ, lexp()).at(given.position());
            default:
                return given;
        }
    }

    private Expression lexp() throws ParserException {
        Expression e = aexp();
        switch (current_rep()){
            case CONS:
                CodePosition cp = current().getPosition();
                advance();
                return new OperatorExpression(e, OperatorExpression.BinaryOperator.CONS, lexp()).at(e.position());
            default: return e;
        }
    }

    private Expression aexp() throws ParserException {
        return aexp_(mexp());
    }


    private Expression aexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case PLUS:
                advance();
                return aexp_(new OperatorExpression(given, OperatorExpression.BinaryOperator.ADD, aexp()).at(given.position()));
            case MINUS:
                advance();
                return aexp_(new OperatorExpression(given, OperatorExpression.BinaryOperator.SUB, aexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression mexp() throws ParserException {
        return mexp_(uexp());
    }


    private Expression mexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case STAR:
                advance();
                return mexp_(new OperatorExpression(given, OperatorExpression.BinaryOperator.MUL, mexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression uexp() throws ParserException {
        switch (current_rep()){
            case PLUS:
                advance();
                return uexp();
            case MINUS:
                CodePosition cp = current().getPosition();
                advance();
                return new OperatorExpression(OperatorExpression.UnaryOperator.NEG, uexp()).at(cp);
            case NOT:
                cp = current().getPosition();
                advance();
                return new OperatorExpression(OperatorExpression.UnaryOperator.NOT, uexp()).at(cp);
            case TYPE_PROJ:
                cp = current().getPosition();
                Token.IntegerToken it = (Token.IntegerToken) current();
                advance();
                try{
                    return new ProjectionExpression(it.value().intValueExact(), uexp()).at(cp);
                }catch (ArithmeticException e){
                    throw new ParserException(it, "Projection index too large!");
                }
            default:
                return sexp();
        }
    }

    private Expression sexp() throws ParserException {
        return sexp_(pexp());
    }


    private Expression sexp_(Expression given) throws ParserException {
        switch (current_rep()){
            case TRUE:
            case FALSE:
            case CLPAR:
            case LPAR:
            case TYPE_INTEGER:
            case TYPE_IDENT:
                return sexp_(new ApplicationExpression(given, pexp()).at(given.position()));
            default:
                return given;
        }
    }

    private Expression pexp() throws ParserException {
        Token current = current();
        switch (current_rep()){
            case TYPE_IDENT:
                advance();
                return new VariableExpression(((Token.IDToken) current).value()).at(current.getPosition());
            case LPAR:
                advance();
                if(current_rep() == RPAR){
                    advance();
                    return new TupleExpression(/*unit*/).at(current.getPosition());
                }
                else{
                    List<Expression> exprs = new LinkedList<>();
                    loop: while(true){
                        exprs.add(exp());
                        switch (current_rep()){
                            case COMMA:
                                advance();
                                continue;
                            case RPAR:
                                advance();
                                break loop;
                        }
                    }
                    if(exprs.size() == 1) return exprs.get(0);
                    return new TupleExpression(exprs).at(current.getPosition());
                }
            case CLPAR:
                advance();
                Datatype.DatatypeDef.DatatypeConstr c = env.isDatatypeConstr("op::").get();
                if(current_rep() == CRPAR){
                    advance();
                    return new ConstantExpression(new AtomValue(env.getDatatypeDef("list"), "nil"));
                }
                else{
                    List<Expression> exprs = new LinkedList<>();
                    loop: while(true){
                        exprs.add(exp());
                        switch (current_rep()){
                            case COMMA:
                                advance();
                                continue;
                            case CRPAR:
                                advance();
                                break loop;
                        }
                    }
                    return new ListExpression(exprs).at(current.getPosition());
                }
            case OP:
                advance();
                switch (current_rep()){
                    case PLUS:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.ADD).at(current.getPosition());
                    case MINUS:
                        advance();
                        if(current_rep() == MINUS){
                            advance();
                            return new OperatorFunctionExpression(OperatorExpression.UnaryOperator.NEG).at(current.getPosition());
                        }
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.SUB).at(current.getPosition());
                    case STAR:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.MUL).at(current.getPosition());
                    case EQUAL:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.EQUAL).at(current.getPosition());
                    case UNEQUAL:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.UNEQUAL).at(current.getPosition());
                    case LESS:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.LESS).at(current.getPosition());
                    case LESSEQUAL:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.LEQ).at(current.getPosition());
                    case GREATER:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.GREATER).at(current.getPosition());
                    case GREATEREQUAL:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.GEQ).at(current.getPosition());
                    case AND:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.AND).at(current.getPosition());
                    case OR:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.OR).at(current.getPosition());
                    case XOR:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.XOR).at(current.getPosition());
                    case CONS:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.BinaryOperator.CONS).at(current.getPosition());
                    case NOT:
                        advance();
                        return new OperatorFunctionExpression(OperatorExpression.UnaryOperator.NOT).at(current.getPosition());
                    default:
                        throw new ParserException(current(), "Expected an operator");
                }
            default:
                try {
                    return new ConstantExpression(p.con()).at(current.getPosition());
                }catch (ParserException p){
                    throw new ParserException(p.getToken(), "Expected an expression");
                }
        }
    }
}
