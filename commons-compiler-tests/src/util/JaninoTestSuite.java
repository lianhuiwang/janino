
/*
 * Janino - An embedded Java[TM] compiler
 *
 * Copyright (c) 2001-2010, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package util;

import java.io.*;

import junit.framework.*;

import org.codehaus.commons.compiler.*;

public class JaninoTestSuite extends StructuredTestSuite {

    private final ICompilerFactory compilerFactory;

    /** The test is expected to throw a ScanException @deprecated */
    public static final CompileAndExecuteTest.Mode SCAN = new CompileAndExecuteTest.Mode();
    /** The test is expected to throw a ParseException @deprecated */
    public static final CompileAndExecuteTest.Mode PARS = new CompileAndExecuteTest.Mode();
    /** The test is expected to throw a CompileException */
    public static final CompileAndExecuteTest.Mode COMP = new CompileAndExecuteTest.Mode();
    /** The test is expected to compile successfully, but is not executed */
    public static final CompileAndExecuteTest.Mode COOK = new CompileAndExecuteTest.Mode();
    /** The test is expected to compile and execute successfully */
    public static final CompileAndExecuteTest.Mode EXEC = new CompileAndExecuteTest.Mode();
    /** The test is expected to compile and execute successfully, and return {@code true} */
    public static final CompileAndExecuteTest.Mode TRUE = new CompileAndExecuteTest.Mode();

    public JaninoTestSuite(String name, ICompilerFactory compilerFactory) {
        super(name);
        this.compilerFactory = compilerFactory;
    }

    /**
     * Add a test case that scans, parses and compiles an expression, and verifies that it
     * evaluates to {@code true}.
     *
     * <table>
     *   <tr><th>{@code mode}</th><th>Meaning</th></tr>
     *   <tr><td>COMP</td><td>The test is expected to throw a CompileException</td></tr>
     *   <tr><td>COOK</td><td>The test is expected to compile successfully, but is not executed</td></tr>
     *   <tr><td>EXEC</td><td>The test is expected to compile and execute successfully</td></tr>
     *   <tr><td>TRUE</td><td>The test is expected to compile and execute successfully, and return {@code true}</tr>
     * </table>
     *
     * @param name The name of the JUnit test case
     */
    protected ExpressionTest exp(ExpressionTest.Mode mode, String name, String expression) throws Exception {
        ExpressionTest et = new ExpressionTest(mode, name, expression);
        addTest(et);
        return et;
    }
    protected class ExpressionTest extends CompileAndExecuteTest {
        private final String               expression;
        private final IExpressionEvaluator expressionEvaluator;

        public ExpressionTest(Mode mode, String name, String expression) throws Exception {
            super(name, mode);
            this.expression          = expression;
            this.expressionEvaluator = compilerFactory.newExpressionEvaluator();

            this.expressionEvaluator.setExpressionType(mode == TRUE ? boolean.class : IExpressionEvaluator.ANY_TYPE);
        }
        public ExpressionTest setDefaultImports(String[] defaultImports) {
            this.expressionEvaluator.setDefaultImports(defaultImports);
            return this;
        }

        protected void compile() throws Exception {
            this.expressionEvaluator.cook(this.expression);
        }

        protected Object execute() throws Exception {
            return this.expressionEvaluator.evaluate(new Object[0]);
        }
    }

    /**
     * Add a test case that scans, parses, compiles and executes a Janino script, and verifies
     * that it returns {@code true}.
     *
     * <table>
     *   <tr><th>{@code mode}</th><th>Meaning</th></tr>
     *   <tr><td>COMP</td><td>The test is expected to throw a CompileException</tr>
     *   <tr><td>COOK</td><td>The test is expected to compile successfully, but is not executed</tr>
     *   <tr><td>EXEC</td><td>The test is expected to compile and execute successfully</tr>
     *   <tr><td>TRUE</td><td>The test is expected to compile and execute successfully, and return {@code true}</tr>
     * </table>
     *
     * @param name The name of the JUnit test case
     */
    protected ScriptTest scr(ScriptTest.Mode mode, String name, String script) throws Exception {
        ScriptTest st = new ScriptTest(mode, name, script);
        addTest(st);
        return st;
    }
    protected class ScriptTest extends CompileAndExecuteTest {
        private final String           script;
        private final IScriptEvaluator scriptEvaluator;

        public ScriptTest(Mode mode, String name, String script) throws Exception {
            super(name, mode);
            this.script          = script;
            this.scriptEvaluator = compilerFactory.newScriptEvaluator();

            this.scriptEvaluator.setReturnType(mode == TRUE ? boolean.class : void.class);
        }
        public ScriptTest setDefaultImports(String[] defaultImports) {
            this.scriptEvaluator.setDefaultImports(defaultImports);
            return this;
        }

        protected void compile() throws Exception {
            this.scriptEvaluator.cook(this.script);
        }

        protected Object execute() throws Exception {
            return this.scriptEvaluator.evaluate(new Object[0]);
        }
    }

    /**
     * Add a test case that scans, parses and compiles a class body, invokes its {@code public static boolean main()}
     * method and verifies that it returns {@code true}.
     *
     * <table>
     *   <tr><th>{@code mode}</th><th>Meaning</th></tr>
     *   <tr><td>COMP</td><td>The test is expected to throw a CompileException</tr>
     *   <tr><td>COOK</td><td>The test is expected to compile successfully, but is not executed</tr>
     *   <tr><td>EXEC</td><td>The test is expected to compile and execute successfully</tr>
     *   <tr><td>TRUE</td><td>The test is expected to compile and execute successfully, and return {@code true}</tr>
     * </table>
     *
     * @param name The name of the JUnit test case
     */
    protected ClassBodyTest clb(ClassBodyTest.Mode mode, String name, String classBody) throws Exception {
        ClassBodyTest cbt = new ClassBodyTest(name, mode, classBody);
        addTest(cbt);
        return cbt;
    }
    protected class ClassBodyTest extends CompileAndExecuteTest {
        private final String              classBody;
        private final IClassBodyEvaluator classBodyEvaluator;

        public ClassBodyTest(String name, Mode mode, String classBody) throws Exception {
            super(name, mode);
            this.classBody          = classBody;
            this.classBodyEvaluator = compilerFactory.newClassBodyEvaluator();
        }
        public ClassBodyTest setDefaultImports(String[] defaultImports) {
            this.classBodyEvaluator.setDefaultImports(defaultImports);
            return this;
        }

        protected void compile() throws Exception {
            this.classBodyEvaluator.cook(this.classBody);
        }

        @SuppressWarnings("unchecked")
        protected Object execute() throws Exception {
            return this.classBodyEvaluator.getClazz().getMethod("main", new Class[0]).invoke(null, new Object[0]);
        }
    }

    /**
     * Add a test case that scans, parses and compiles a compilation unit, then calls the {@code public static boolean
     * test()} method of the named class, and verifies that it returns {@code true}.
     *
     * <table>
     *   <tr><th>{@code mode}</th><th>Meaning</th></tr>
     *   <tr><td>COMP</td><td>The test is expected to throw a CompileException</tr>
     *   <tr><td>COOK</td><td>The test is expected to compile successfully, but is not executed</tr>
     *   <tr><td>EXEC</td><td>The test is expected to compile and execute successfully</tr>
     *   <tr><td>TRUE</td><td>The test is expected to compile and execute successfully, and return {@code true}</tr>
     * </table>
     *
     * @param name The name of the JUnit test case
     * @param className The name of the class with the {@code public static boolean test()} method
     */
    protected SimpleCompilerTest sim(
        SimpleCompilerTest.Mode mode,
        String                  name,
        String                  compilationUnit,
        String                  className
    ) throws Exception {
        SimpleCompilerTest sct = new SimpleCompilerTest(name, mode, compilationUnit, className);
        addTest(sct);
        return sct;
    }
    protected class SimpleCompilerTest extends CompileAndExecuteTest {
        private final String          compilationUnit;
        private final String          className;
        private final ISimpleCompiler simpleCompiler;

        public SimpleCompilerTest(String name, Mode mode, String compilationUnit, String className) throws Exception {
            super(name, mode);
            this.compilationUnit = compilationUnit;
            this.className       = className;
            this.simpleCompiler  = compilerFactory.newSimpleCompiler();
        }

        protected void compile() throws Exception {
            this.simpleCompiler.cook(this.compilationUnit);
        }

        protected Object execute() throws Exception {
            return (
                this.simpleCompiler.getClassLoader()
                .loadClass(this.className)
                .getMethod("test", new Class[0])
                .invoke(null, new Object[0])
            );
        }
    }

    /**
     * Create and return a test case that sets up a {@link JavaSourceClassLoader} that reads sources from a given
     * directory, and then loads the named class.
     *
     * @param className The name of the class to be loaded from the {@link JavaSourceClassLoader}
     */
    protected void jscl(String testCaseName, final File sourceDirectory, final String className) {
        TestCase testCase = new TestCase(testCaseName) {
            protected void runTest() throws Throwable {
                AbstractJavaSourceClassLoader loader = compilerFactory.newJavaSourceClassLoader();
                loader.setSourcePath(new File[] { sourceDirectory });
                loader.loadClass(className);
            }
        };
        addTest(testCase);
    }

    /**
     * A test case that calls its abstract methods {@link #compile()}, then {@link #execute()}, and
     * verifies that they throw exceptions and return results as expected.
     */
    abstract static class CompileAndExecuteTest extends BenchmarkingTestCase {
        protected final Mode mode;

        public static final class Mode { private Mode() {} }

        public CompileAndExecuteTest(String name, Mode mode) {
            super(name);
            // Notice: JUnit 3.8.1 gets confused if the name contains "(" and/or ",".
            if (name.indexOf('(') != -1) throw new RuntimeException("Parentheses in test name not permitted");
            if (name.indexOf(',') != -1) throw new RuntimeException("Comma in test name not permitted");
            this.mode = mode;
        }

        protected abstract void   compile() throws Exception;
        protected abstract Object execute() throws Exception;

        /**
         * Invoke {@link #compile()}, then {@link #execute()}, and verify that they throw exceptions and return
         * results as expected.
         */
        protected void runTest() throws Exception {
            if (this.mode == COMP || this.mode == PARS || this.mode == SCAN) {
                try {
                    this.compile();
                } catch (CompileException ex) {
                    return;
                } catch (LocatedException le) {
                    assertEquals("CompileException", le);
                }
                fail("Should have thrown CompileException, but compiled successfully");
            } else
            if (this.mode == COOK) {
                this.compile();
            } else
            if (this.mode == EXEC) {
                this.compile();
                this.execute();
            } else
            if (this.mode == TRUE) {
                this.compile();
                Object result = this.execute();
                assertNotNull("Test result", result);
                assertSame("Test return type", Boolean.class, result.getClass());
                assertEquals("Test result", true, ((Boolean) result).booleanValue());
            } else
            {
                fail("Invalid mode \"" + this.mode + "\"");
            }
        }
    }
}
