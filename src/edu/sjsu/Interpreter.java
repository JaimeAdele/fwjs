package edu.sjsu;

public class Interpreter {

    public static void main(String[] args) throws Exception {
//        Expression prog = new BinOpExpr(Op.ADD,
//                new ValueExpr(new IntVal(3)),
//                new ValueExpr(new IntVal(4)));
        Expression prog = new SeqExpr(
                new VarDeclExpr("v", new ValueExpr(new IntVal(3))),
                new VarExpr("v"));
        System.out.println("'int v = 3; v;' evaluates to " + prog.evaluate(new Environment()));
    }
}