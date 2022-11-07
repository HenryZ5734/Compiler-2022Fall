package cn.edu.hitsz.compiler.asm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author henry
 */
public class Reg {
    private static final Reg reg = new Reg();
    private int size;
    private final String[] freeRegs = {"t0", "t1", "t2", "t3", "t4", "t5", "t6"};

    private Reg(){
        size = freeRegs.length;
    }

    public static Reg getInstance(){
        return reg;
    }

    public boolean hasFreeReg(){
        return (size!=0);
    }

    public String getFreeReg(){
        return freeRegs[--size];
    }

    public void addFreeReg(String regNum){
        freeRegs[size++] = regNum;
    }
}
