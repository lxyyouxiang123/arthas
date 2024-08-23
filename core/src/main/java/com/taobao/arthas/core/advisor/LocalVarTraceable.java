package com.taobao.arthas.core.advisor;

/**
 * 局部变量跟踪
 */
public interface LocalVarTraceable  extends MethodInnerTraceable{


    /**
     *  每行通知
     * @param clazz 类
     * @param target 目标类实例
     * @param methodName 方法名称
     * @param methodDesc 方法描述
     * @param localVars 局部变量值
     * @param localNames 局部变量名称
     * @param line 行号
     */
    void perLine(Class<?> clazz, Object target, String methodName, String methodDesc, Object[] localVars, String[] localNames, Integer line);


}
