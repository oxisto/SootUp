package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.util.printer.Printer;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 * 
 * @author Linghui Luo
 *
 */

@Category(Java8Test.class)
public class IfInstructionConversionTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
    declareClassSig = sigFactory.getClassSignature("de.upb.soot.concrete.controlStatements.ControlStatements");
  }

  @Test
  public void test1() {
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("simpleIfElseIfTakeThen", declareClassSig, "void",
            Arrays.asList("int", "int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test2() {
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("simpleIfElse",
        declareClassSig, "boolean",
        Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test3() {  
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("simpleIfElse",
        declareClassSig, "boolean",
        Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test4() {
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("simpleIf",
        declareClassSig, "boolean",
        Collections.singletonList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test5()
  {
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("simpleIfElseIfTakeElse",
        declareClassSig,
        "void", Arrays.asList("double", "double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

}