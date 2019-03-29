package de.jojomodding.lang.ast;

import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.parsing.CodePosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ASTElement {

    private CodePosition where;

    public CodePosition position(){
        return where;
    }

    public ASTElement at(CodePosition p){
        if(p == null) throw new NullPointerException("p is null!");
        this.where = p;
        return this;
    }

    protected void asChildren(ASTElement... childs){
        Arrays.stream(childs).
                peek(children::add).
                forEach(p -> p.setParent(this));
    }

    private void setParent(ASTElement p){
        this.parent = p;
    }

    public ASTElement getParent(){
        return parent;
    }

    public Stream<ASTElement> children(){
        return children.stream();
    }

    private ASTElement parent = null;
    private List<ASTElement> children = new ArrayList<>();

}
