package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    private final List<Instruction> preProcessInstructions = new LinkedList<>();
    private final Map<IRValue, String> map = new HashMap<>();
    private final List<Asm> asmList = new LinkedList<>();

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        for(var i : originInstructions){
            // RET 和 MOV 指令无需处理
            if(i.getKind().isReturn() || i.getKind().isUnary()){
                preProcessInstructions.add(i);
            }
            else{
                IRValue left=i.getLHS();
                IRValue right=i.getRHS();
                // 对应左右操作数都是立即数以及只有左操作数是立即数且为SUB命令两种情况
                if(left.isImmediate() && (right.isImmediate() || i.getKind() == InstructionKind.SUB)){
                    IRVariable result = IRVariable.temp();
                    preProcessInstructions.add(Instruction.createMov(result, left));
                    switch(i.getKind()){
                        case ADD -> preProcessInstructions.add(Instruction.createAdd(i.getResult(), result, i.getRHS()));
                        case SUB -> preProcessInstructions.add(Instruction.createSub(i.getResult(), result, i.getRHS()));
                        case MUL -> preProcessInstructions.add(Instruction.createMul(i.getResult(), result, i.getRHS()));
                    }
                }
                // 对应只有左操作数是立即数且非SUB命令的情况
                else if(left.isImmediate()){
                    switch(i.getKind()){
                        case ADD -> preProcessInstructions.add(Instruction.createAdd(i.getResult(), i.getRHS(), i.getLHS()));
                        case MUL -> preProcessInstructions.add(Instruction.createMul(i.getResult(), i.getRHS(), i.getLHS()));
                    }
                }
                // 对应只有右操作数是立即数的情况
                else{
                    preProcessInstructions.add(i);
                }
            }
        }
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        Reg reg = Reg.getInstance();
        for(var i : preProcessInstructions){
            System.out.println(i);
            // 无返回值
            // RET
            if(i.getKind().isReturn()){
                asmList.add(Asm.createMv("a0", map.get(i.getReturnValue())));
            }
            // 有返回值
            else{
                IRValue result = i.getResult();
                // 首先为结果分配寄存器
                if(!map.containsKey(result)){
                    if(reg.hasFreeReg()){
                        map.put(i.getResult(), reg.getFreeReg());
                    }
                    else{
                        System.out.println("没有足够的寄存器");
                    }
                }

                // MOV
               if(i.getKind().isUnary()){
                    // 有立即数
                    if(i.getFrom().isImmediate()){
                        asmList.add(Asm.createLi(map.get(result), ((IRImmediate)i.getFrom()).getValue()));
                    }
                    // 无立即数
                    else{
                        asmList.add(Asm.createMv(map.get(result), map.get(i.getFrom())));
                        // 若临时变量被用过，则存放该临时变量的寄存器应当被释放
                        if(((IRVariable)i.getFrom()).isTemp()){
                            reg.addFreeReg(map.remove(i.getFrom()));
                        }
                    }
               }
               else if(i.getKind() == InstructionKind.ADD){
                    // ADDI
                    if(i.getRHS().isImmediate()){
                        asmList.add(Asm.createAddi(map.get(result), map.get(i.getLHS()), ((IRImmediate)i.getRHS()).getValue()));
                        if(((IRVariable)i.getLHS()).isTemp()){
                            reg.addFreeReg(map.remove(i.getLHS()));
                        }
                    }
                    // ADD
                    else{
                        asmList.add(Asm.createAdd(map.get(result), map.get(i.getLHS()), map.get(i.getRHS())));
                        if(((IRVariable)i.getLHS()).isTemp()){
                            reg.addFreeReg(map.remove(i.getLHS()));
                        }
                        if(((IRVariable)i.getRHS()).isTemp()){
                            reg.addFreeReg(map.remove(i.getRHS()));
                        }
                    }

               }
               else if(i.getKind() == InstructionKind.SUB){
                    // SUBI
                   if(i.getRHS().isImmediate()){
                       asmList.add(Asm.createSubi(map.get(result), map.get(i.getLHS()), ((IRImmediate)i.getRHS()).getValue()));
                       if(((IRVariable)i.getLHS()).isTemp()){
                           reg.addFreeReg(map.remove(i.getLHS()));
                       }
                   }
                   // SUB
                   else{
                       asmList.add(Asm.createSub(map.get(result), map.get(i.getLHS()), map.get(i.getRHS())));
                       if(((IRVariable)i.getLHS()).isTemp()){
                           reg.addFreeReg(map.remove(i.getLHS()));
                       }
                       if(((IRVariable)i.getRHS()).isTemp()){
                           reg.addFreeReg(map.remove(i.getRHS()));
                       }
                   }
               }
               else if(i.getKind() == InstructionKind.MUL){
                    // MULI
                   if(i.getRHS().isImmediate()){
                       asmList.add(Asm.createMuli(map.get(result), map.get(i.getLHS()), ((IRImmediate)i.getRHS()).getValue()));
                       if(((IRVariable)i.getLHS()).isTemp()){
                           reg.addFreeReg(map.remove(i.getLHS()));
                       }
                   }
                   // MUL
                   else{
                       asmList.add(Asm.createMul(map.get(result), map.get(i.getLHS()), map.get(i.getRHS())));
                       if(((IRVariable)i.getLHS()).isTemp()){
                           reg.addFreeReg(map.remove(i.getLHS()));
                       }
                       if(((IRVariable)i.getRHS()).isTemp()){
                           reg.addFreeReg(map.remove(i.getRHS()));
                       }
                   }
               }
            }
            System.out.println(asmList.get(asmList.size()-1));
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        List<String> lines = new LinkedList<>();
        lines.add(".text");
        lines.addAll(asmList.stream().map(Asm::toString).toList());
        FileUtils.writeLines(path, lines);
    }
}

