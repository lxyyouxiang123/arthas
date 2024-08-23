package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.SearchUtils;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.arthas.core.view.ObjectView;
import com.taobao.middleware.cli.annotations.*;


@Name("localvars")
@Summary("Methods: Observation of local variables")
@Description(value =Constants.EXAMPLE +
        "  localvars demo.MathGame run -n 5 -ml 20 -sl 37\n" +
        Constants.WIKI + Constants.WIKI_HOME + "localvars")
public class LocalVarsCommand extends EnhancerCommand{

    private String className;
    private String methodName;

    private String localVarsExpress;

    private String conditionExpress;

    private int numberOfLimit = 100;


    private int expand=1;


    private Integer sizeLimit = 10 * 1024 * 1024;

    /**
     * 开始行号
     */
    private int startLine=0;

    /**
     * 最大打印行数
     */
    private int lineOfLimit=20;

    /**
     * 本地变量名称
     */
    private String[] varNames;

    @Argument(argName = "class-name", index = 0)
    @Description("Class name  use either '.' or '/' as separator")
    public void setClassName(String className) {
        this.className = className;
    }

    @Argument(argName = "method-name", index = 1)
    @Description("Method name")
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    @Argument(index = 2, argName = "localvars-express", required = false)
    @DefaultValue("")
    @Description("The name of the local variable used to display. The format is as follows: \"#{'newLocalVarName1':#this.LocalVarName1,'newLocalVarName2':#this.LocalVarName2}\", please use commas to separate without spaces;If it is empty, it means that all are displayed")
    public void setLocalVarsExpress(String localVarsExpress) {
        this.localVarsExpress = localVarsExpress;
    }

    @Argument(index = 3, argName = "condition-express", required = false)
    @Description(Constants.CONDITION_EXPRESS)
    public void setConditionExpress(String conditionExpress) {
        this.conditionExpress = conditionExpress;
    }

    @Option(shortName = "n", longName = "limits")
    @Description("Threshold of execution times")
    public void setNumberOfLimit(int numberOfLimit) {
        this.numberOfLimit = numberOfLimit;
    }


    @Option(shortName = "sl", longName = "start-line")
    @Description("Start line number")
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }


    @Option(shortName = "ml", longName = "line-limits")
    @Description("Limit on the number of lines")
    public void setLineOfLimit(int lineOfLimit) {
        this.lineOfLimit = lineOfLimit;
    }




    @Option(shortName = "x", longName = "expand")
    @Description("Expand level of object (1 by default), the max value is " + ObjectView.MAX_DEEP)
    public void setExpand(Integer expand) {
        this.expand = expand;
    }


    @Option(shortName = "M", longName = "sizeLimit")
    @Description("Upper size limit in bytes for the result (10 * 1024 * 1024 by default)")
    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public String getLocalVarsExpress() {
        return localVarsExpress;
    }

    public String getConditionExpress() {
        return conditionExpress;
    }

    public Integer getSizeLimit() {
        return sizeLimit;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }


    public int getNumberOfLimit() {
        return numberOfLimit;
    }


    public int getStartLine() {
        return startLine;
    }

    public int getLineOfLimit() {
        return lineOfLimit;
    }


    public Integer getExpand() {
        return expand;
    }


    @Override
    protected Matcher getClassNameMatcher() {
        if (classNameMatcher == null) {
            classNameMatcher = SearchUtils.classNameMatcher(getClassName(), false);
        }
        return classNameMatcher;
    }

    @Override
    protected Matcher getClassNameExcludeMatcher() {
        return classNameExcludeMatcher;
    }

    @Override
    protected Matcher getMethodNameMatcher() {
        if (methodNameMatcher == null) {
            methodNameMatcher = SearchUtils.classNameMatcher(getMethodName(), false);
        }
        return methodNameMatcher;
    }

    @Override
    protected AdviceListener getAdviceListener(CommandProcess process) {
        return new LocalVarsAdviceListener(this,process, GlobalOptions.verbose || this.verbose);
    }

}
