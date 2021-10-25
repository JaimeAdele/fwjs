package edu.sjsu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
    private Value val;
    public ValueExpr(Value v) {
        this.val = v;
    }
    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;
    public VarExpr(String varName) {
        this.varName = varName;
    }
    public Value evaluate(Environment env) {
        return env.resolveVar(varName);
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;
    public PrintExpr(Expression exp) {
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2;
    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @SuppressWarnings("incomplete-switch")
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
        Value val1 = this.e1.evaluate(env);
        Value val2 = this.e2.evaluate(env);
        if (val1 instanceof IntVal && val2 instanceof IntVal) {
            int int1 = ((IntVal) val1).toInt();
            int int2 = ((IntVal) val2).toInt();
            switch (this.op) {
                case ADD:
                    return new IntVal(int1 + int2);
                case SUBTRACT:
                    return new IntVal(int1 - int2);
                case MULTIPLY:
                    return new IntVal(int1 * int2);
                case DIVIDE:
                    return new IntVal(int1 / int2);
                case MOD:
                    return new IntVal(int1 % int2);
                case GT:
                    return new BoolVal(int1 > int2);
                case GE:
                    return new BoolVal(int1 >= int2);
                case LT:
                    return new BoolVal(int1 < int2);
                case LE:
                    return new BoolVal(int1 <= int2);
                case EQ:
                    return new BoolVal(int1 == int2);
            }
        }
        return null;
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els;
    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }
//    public Value evaluate(Environment env) {
//        // YOUR CODE HERE
//        Value condition = this.cond.evaluate(env);
//        if (condition instanceof BoolVal && ((BoolVal) condition).toBoolean()) {
//            return this.thn.evaluate(env);
//        } else {
//            return this.els.evaluate(env);
//        }
//    }
    
    public Value evaluate(Environment env) {
       // YOUR CODE HERE
       Value condition = this.cond.evaluate(env);
       if (!(condition instanceof BoolVal))
           throw new IllegalArgumentException("Condition should be BoolVal");
       if (((BoolVal) condition).toBoolean()) {
           return this.thn.evaluate(env);
       } else {
           return this.els.evaluate(env);
       }
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;
    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
        Value condition = this.cond.evaluate(env);
        Value returnVal = null;
        if (condition instanceof BoolVal) {
            while (((BoolVal) condition).toBoolean()) {
                returnVal = this.body.evaluate(env);
                condition = this.cond.evaluate(env);
            }
        }
        return returnVal;
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;
    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
        this.e1.evaluate(env);
        return this.e2.evaluate(env);
        //return null;
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;
    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
        env.createVar(varName, exp.evaluate(env));//not sure about the value here... --------------------------------
        return null;
    }
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;
    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
        Value varVal = e.evaluate(env);
        env.updateVar(varName, varVal);
        return varVal;
    }
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;
    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
        return new ClosureVal(this.params, this.body, env);
    }
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;
    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }
    public Value evaluate(Environment env) {
        // YOUR CODE HERE
        ClosureVal closureVal = (ClosureVal)f.evaluate(env);//this should be a closure
        //Create an iterator to evaluate each item in the args list
        Iterator<Expression> argsIterator = this.args.iterator();
        List<Value> argsVals = new ArrayList<>();
        while (argsIterator.hasNext()) {
            argsVals.add(argsIterator.next().evaluate(env));
        }
        return closureVal.apply(argsVals);
        //return null;
    }
}
