package de.jojomodding.lang.parsing.parser;

import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.ast.pattern.*;
import de.jojomodding.lang.exception.ParserException;
import de.jojomodding.lang.parsing.CodePosition;
import de.jojomodding.lang.parsing.Token;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.ConstantValue;

import java.util.*;

import static de.jojomodding.lang.parsing.Token.Basic.*;

public class PatternHelper extends ParserHelper<Pattern> {

    public PatternHelper(Parser p) {
        super("pat", p);
    }

    @Override
    public Pattern parse() throws ParserException {
        return pat();
    }

    public PatternRow patrow() throws ParserException {
        CodePosition cp = current().getPosition();
        List<Map.Entry<Pattern, Expression>> l = new LinkedList<>();
        loop: while(true){
            Pattern p = pat();
            if (current_rep() == BIGARROW) {
                advance();
            } else {
                throw new ParserException(current(), "Expected \"=>\"");
            }
            l.add(Map.entry(p, this.p.exp()));
            switch (current_rep()){
                case VBAR:
                    advance();
                    continue loop;
                default: break loop;
            }
        }
        return (PatternRow) new PatternRow(l).at(cp);
    }

    public PatternList patlist() throws ParserException {
        CodePosition cp = current().getPosition();
        List<Pattern> pl = new LinkedList<>();
        loop: while (true){
            switch (current_rep()){
                case TYPE_IDENT:
                case WILDCARD:
                case LPAR:
                case CLPAR:
                case TYPE_INTEGER:
                case TRUE:
                case FALSE:
                    pl.add(ppat());
                    continue loop;
                default:
                    break loop;
            }
        }
        if(pl.size() == 0)
            throw new ParserException(current(), "Expected a pattern");
        return new PatternList(pl);
    }

    private Pattern pat() throws ParserException {
        Pattern otr = apat();
        switch (current_rep()){
            case COLON:
                advance();
                Type t = this.p.ty();
                return new AnnotatedPattern(otr, t).at(otr.position());
            default: return otr;
        }
    }

    private Pattern apat() throws ParserException {
        Pattern p = cpat();
        switch (current_rep()){
            case AS:
                advance();
                return new AsPattern(p, cpat()).at(p.position());
            default: return p;
        }
    }


    private Pattern cpat() throws ParserException {
        if(current_rep() == TYPE_IDENT){
            String v = ((Token.IDToken)current()).value();
            CodePosition cp = current().getPosition();
            Optional<Datatype.DatatypeDef.DatatypeConstr> oc = env.isDatatypeConstr(v);
            if(oc.isPresent()){
                advance();
                Pattern p = lpat();
                return new ConstructorPattern(oc.get(), p).at(cp);
            }
        }
        return lpat();
    }

    private Pattern lpat() throws ParserException {
        Pattern p = ppat();
        if(current_rep() == CONS){
            advance();
            Pattern op = lpat();
            Datatype.DatatypeDef.DatatypeConstr lc = env.isDatatypeConstr("op::").get();
            return new ConstructorPattern(lc, new TuplePattern(p, op));
        }
        return p;
    }

    private Pattern ppat() throws ParserException {
        CodePosition cp = current().getPosition();
        switch (current_rep()){
            case TYPE_IDENT:
                String s = ((Token.IDToken)current()).value();
                Optional<Datatype.DatatypeDef> od = env.isDatatypeAtom(s);
                if(od.isPresent()){
                    advance();
                    return new AtomPattern(od.get(), s);
                }
                if(env.isDatatypeConstr(s).isPresent()){
                    throw new ParserException(current(), "Constructor cannot be used as variable");
                }
                advance();
                return new VariablePattern(s, env.newType());
            case WILDCARD:
                advance();
                return new WildcardPattern(env.newType()).at(cp);
            case TYPE_INTEGER:
            case TRUE:
            case FALSE:
                ConstantValue cv = this.p.con();
                return new ConstantPattern(cv).at(cp);
            case LPAR:
                List<Pattern> pl = new ArrayList<>();
                advance();
                if(current_rep() != RPAR)
                    loop: while(true){
                        pl.add(pat());
                        switch (current_rep()){
                            case COMMA:
                                advance();
                                continue loop;
                            case RPAR:
                                advance();
                                break loop;
                            default: throw new ParserException(current(), "expected \")\" or \",\"");
                        }
                    }
                else advance();
                if(pl.size() == 1)return pl.get(0);
                return new TuplePattern(pl).at(cp);
            case CLPAR:
                pl = new ArrayList<>();
                advance();
                if(current_rep() != CRPAR)
                    loop: while(true){
                        pl.add(pat());
                        switch (current_rep()){
                            case COMMA:
                                advance();
                                continue loop;
                            case CRPAR:
                                advance();
                                break loop;
                            default: throw new ParserException(current(), "expected \"]\" or \",\"");
                        }
                    }
                else advance();
                return new ListPattern(pl).at(cp);
            default: throw new ParserException(current(), "Expected a pattern");
        }
    }
}
