package de.jojomodding.lang.parsing.parser;

import de.jojomodding.lang.exception.ParserException;
import de.jojomodding.lang.parsing.Token;
import de.jojomodding.lang.type.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static de.jojomodding.lang.parsing.Token.Basic.ARROW;
import static de.jojomodding.lang.parsing.Token.Basic.TYPE_IDENT;

public class TypeParser extends ParserHelper<Type> {

    public TypeParser(Parser p) {
        super("ty", p);
    }

    @Override
    public Type parse() throws ParserException {
        return ty();
    }

    private Type ty() throws ParserException {
        Type pty = sty();
        if(current_rep()== ARROW){
            advance();
            Type nty = ty();
            return new FunctionType(pty, nty);
        }
        return pty;
    }

    private Type sty() throws ParserException {
        List<Type> lt = new LinkedList<>();
        loop: while (true){
            lt.add(dty());
            switch (current_rep()){
                case STAR:
                    advance();
                    continue;
                default: break loop;
            }
        }
        if(lt.size() == 1) return lt.get(0);
        return new TupleType(lt.toArray(new Type[0]));
    }

    private Type dty() throws ParserException {
        return dty_(pty());
    }

    private Type dty_(Type given) throws ParserException {
        if(current_rep() == TYPE_IDENT){
            Datatype.DatatypeDef dtdef = env.getDatatypeDef(((Token.IDToken)current()).value());
            if(dtdef == null) throw new ParserException(current(), "Unknown datatype "+((Token.IDToken)current()).value());
            if(1 != dtdef.getAmountOfSpecifyingTypes()) throw new ParserException(current(), "Type"+dtdef.getName()+" requires "+dtdef.getAmountOfSpecifyingTypes()+" subtypes, 1 given!");
            advance();
            return dty_(new Datatype(dtdef, List.of(given)));
        }else return given;
    }

    private Type pty() throws ParserException {
        switch (current_rep()){
            case INT:
                advance();
                return BaseType.INT;
            case BOOL:
                advance();
                return BaseType.BOOL;
            case LPAR:
                advance();
                Type t = ty();
                switch (current_rep()){
                    case RPAR:
                        advance();
                        return t;
                    case COMMA:
                        advance();
                        List<Type> st = new ArrayList<Type>();
                        st.add(t);
                        loop: while (true){
                            st.add(ty());
                            switch (current_rep()){
                                case COMMA:
                                    advance();
                                    continue loop;
                                case RPAR:
                                    advance();
                                    if(current_rep() != TYPE_IDENT) throw new ParserException(current(), "Expected a custom type name");
                                    Datatype.DatatypeDef dtdef = env.getDatatypeDef(((Token.IDToken)current()).value());
                                    if(dtdef == null) throw new ParserException(current(), "Unknown datatype "+((Token.IDToken)current()).value());
                                    if(st.size() != dtdef.getAmountOfSpecifyingTypes()) throw new ParserException(current(), "Type"+dtdef.getName()+" requires "+dtdef.getAmountOfSpecifyingTypes()+" subtypes, "+st.size()+" given!");
                                    return new Datatype(dtdef, st);
                                default:
                                    throw new ParserException(current(), "expected \")\" or \",\"");
                            }
                        }
                    default:
                        throw new ParserException(current(), "expected \")\" or \",\"");
                }
            case TYPE_TVAR:
                Token.IDToken to = (Token.IDToken) current();
                advance();
                return env.translateUserProvidedVar(to.value());
            case TYPE_TVAR_EQ:
                to = (Token.IDToken) current();
                advance();
                TypeVariable tv = env.translateUserProvidedVar(to.value(), true);
                return env.withEquality(tv);
            case TYPE_IDENT:
                Datatype.DatatypeDef dtdef = env.getDatatypeDef(((Token.IDToken)current()).value());
                if(dtdef == null) throw new ParserException(current(), "Unknown datatype "+((Token.IDToken)current()).value());
                if(0 != dtdef.getAmountOfSpecifyingTypes()) throw new ParserException(current(), "Type"+dtdef.getName()+" requires "+dtdef.getAmountOfSpecifyingTypes()+" subtypes, 0 given!");
                advance();
                return new Datatype(dtdef, List.of());
            default:
                throw new ParserException(current(), "expected \"int\", \"bool\", \"(\" ty \")\" or a type identifier");
        }
    }

}
