package com.taobao.arthas.core.command.model;

import java.util.concurrent.atomic.AtomicInteger;

public class LocalVarsModel extends ResultModel{

    private String className;

    private String methodName;

    private Object[]    methodArgs;

    private Integer line;

    private Object   returnObject;

    private Throwable throwable;

    private int sizeLimit;

    private int accessPoint;

    private int expand=1;

    private StringBuffer showValue;

    private AtomicInteger totalNums=new AtomicInteger(0);
    private Object localVars;

    @Override
    public String getType() {
        return "localvars";
    }

    public Object[] getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(Object[] methodArgs) {
        this.methodArgs = methodArgs;

    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }


    public Object getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public int getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public int getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(int accessPoint) {
        this.accessPoint = accessPoint;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getExpand() {
        return expand;
    }

    public void setExpand(int expand) {
        this.expand = expand;
    }

    public AtomicInteger getTotalNums() {
        return totalNums;
    }

    public void  addLine(int line, Object localVarMap){
        totalNums.incrementAndGet();
        this.line=line;
        this.localVars =localVarMap;
    }

    public Object getLocalVars() {
        return localVars;
    }

    public StringBuffer getShowValue() {
        return showValue;
    }

    public void setShowValue(StringBuffer showValue) {
        this.showValue = showValue;
    }
}
