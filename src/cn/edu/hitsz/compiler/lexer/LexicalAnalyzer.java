package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private final LinkedList<String> lines = new LinkedList<>();
    private final LinkedList<Token> tokens = new LinkedList<>();

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        int current_state;
        String tokenContent;
        // TODO: 自动机实现的词法分析过程
        // i指向当前行第一个未读字符
        // 由于状态1、3、5只起到处理字符的作用，并未读取，所以索引i要先自减，保证指针不动
        for(String line : lines){
            current_state = 0;
            tokenContent = "";
            for(int i=0;i<line.length();i++){
                switch (current_state) {
                    case 0 -> {
                        tokenContent = "" + line.charAt(i);
                        current_state = stateChange0(tokenContent);
                    }
                    case 1 -> {
                        i--;
                        tokens.add(Token.simple(TokenKind.fromString(tokenContent)));
                        current_state = 0;
                    }
                    case 2 -> {
                        while (48 <= line.charAt(i) && line.charAt(i) <= 57) {
                            tokenContent += line.charAt(i++);
                        }
                        i--;
                        current_state = 3;
                    }
                    case 3 -> {
                        i--;
                        tokens.add(Token.normal("IntConst", tokenContent));
                        current_state = 0;
                    }
                    case 4 -> {
                        while ((48 <= line.charAt(i) && line.charAt(i) <= 57) ||
                                ('A' <= line.charAt(i) && line.charAt(i) <= 'Z') ||
                                ('a' <= line.charAt(i) && line.charAt(i) <= 'z')) {
                            tokenContent += line.charAt(i++);
                        }
                        i--;
                        current_state = 5;
                    }
                    case 5 -> {
                        i--;
                        if (tokenContent.equals("int") || tokenContent.equals("return")) {
                            tokens.add(Token.simple(TokenKind.fromString(tokenContent)));
                        } else {
                            tokens.add(Token.normal("id", tokenContent));
                            if(!symbolTable.has(tokenContent)){
                                symbolTable.add(tokenContent);
                            }
                        }
                        current_state = 0;
                    }
                }
            }
            if(tokenContent.equals(";")){
                tokens.add(Token.simple(TokenKind.fromString("Semicolon")));
            }
        }
        tokens.add(Token.simple(TokenKind.eof()));
    }

    public int stateChange0(String in){
        String blank = " \t\n";
        String symbol = "=,;+-*/()";
        String digit = "1234567890";
        if(blank.contains(in)){
            return 0;
        }
        else if(symbol.contains(in)){
            return 1;
        }
        else if(digit.contains(in)){
            return 2;
        }
        else if(in.equals("$")){
            return 6;
        }
        else{
            return 4;
        }
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return tokens;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
