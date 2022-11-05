package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.LinkedList;
import java.util.List;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    private SymbolTable table;
    private final List<Instruction> instructions = new LinkedList<>();
    private final List<IRValue> valueList = new LinkedList<>();
    private final List<Token> tokenList = new LinkedList<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        tokenList.add(currentToken);
        if(currentToken.getKindId().equals("IntConst")){
            valueList.add(IRImmediate.of(Integer.parseInt(currentToken.getText())));
        }
        else {
            valueList.add(IRVariable.named(currentToken.getText()));
        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        LinkedList<Token> token = new LinkedList<>();
        LinkedList<IRValue> value = new LinkedList<>();
        for(int i=0;i<production.body().size();i++){
            token.add(((LinkedList<Token>)tokenList).removeLast());
            value.add(((LinkedList<IRValue>)valueList).removeLast());
        }

        switch(production.index()){
            case 1,2,3,4,5 ->{
                tokenList.add(Token.simple("int"));
                valueList.add(IRVariable.named(""));
            }
            case 6 ->{
                IRVariable result = IRVariable.named(token.get(2).getText());
                IRValue from = value.get(0);
                instructions.add(Instruction.createMov(result, from));
                tokenList.add(Token.simple("int"));
                valueList.add(IRVariable.named(""));
            }
            case 7 ->{
                instructions.add(Instruction.createRet(value.get(0)));
                tokenList.add(Token.simple("int"));
                valueList.add(IRVariable.named(""));
            }
            case 8->{
                IRVariable result = IRVariable.temp();
                instructions.add(Instruction.createAdd(result, value.get(2), value.get(0)));
                tokenList.add(token.get(0));
                valueList.add(result);
            }
            case 9->{
                IRVariable result = IRVariable.temp();
                instructions.add(Instruction.createSub(result, value.get(2), value.get(0)));
                tokenList.add(token.get(0));
                valueList.add(result);
            }
            case 11->{
                IRVariable result = IRVariable.temp();
                instructions.add(Instruction.createMul(result, value.get(2), value.get(0)));
                tokenList.add(token.get(0));
                valueList.add(result);
            }
            case 10, 12, 14, 15->{
                tokenList.add(token.get(0));
                valueList.add(value.get(0));
            }
            case 13->{
                tokenList.add(token.get(1));
                valueList.add(value.get(1));
            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        // do nothing
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.table = table;
    }

    public List<Instruction> getIR() {
        // TODO
        return instructions;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

