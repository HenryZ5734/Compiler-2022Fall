package cn.edu.hitsz.compiler.asm;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author henry
 */
public class Asm {

    public static Asm createLi(String to, int value){
        return new Asm(AsmKind.li, to, List.of(String.valueOf(value)));
    }

    public static Asm createMv(String to, String from){
        return new Asm(AsmKind.mv, to, List.of(from));
    }

    public static Asm createAdd(String to, String from1, String from2){
        return new Asm(AsmKind.add, to, List.of(from1, from2));
    }

    public static Asm createAddi(String to, String from1, int from2){
        return new Asm(AsmKind.addi, to, List.of(from1, String.valueOf(from2)));
    }

    public static Asm createSub(String to, String from1, String from2){
        return new Asm(AsmKind.sub, to, List.of(from1, from2));
    }

    public static Asm createSubi(String to, String from1, int from2){
        return new Asm(AsmKind.subi, to, List.of(from1, String.valueOf(from2)));
    }

    public static Asm createMul(String to, String from1, String from2){
        return new Asm(AsmKind.mul, to, List.of(from1, from2));
    }

    public static Asm createMuli(String to, String from1, int from2){
        return new Asm(AsmKind.muli, to, List.of(from1, String.valueOf(from2)));
    }

    private final AsmKind asmKind;
    private final String to;
    private final List<String> from;

    private Asm(AsmKind asmKind, String to, List<String> from){
        this.asmKind = asmKind;
        this.to = to;
        this.from = from;
    }

    @Override
    public String toString() {
        final var kindString = asmKind.toString();
        final var fromString = from.stream().map(Objects::toString).collect(Collectors.joining(", "));
        return "    %s %s, %s".formatted(kindString, to, fromString);
    }
}
