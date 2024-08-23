package com.taobao.arthas.core.command.monitor200;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.advisor.Advice;
import com.taobao.arthas.core.advisor.AdviceListenerAdapter;
import com.taobao.arthas.core.advisor.ArthasMethod;
import com.taobao.arthas.core.advisor.LocalVarTraceable;
import com.taobao.arthas.core.command.express.ExpressException;
import com.taobao.arthas.core.command.model.LocalVarsModel;
import com.taobao.arthas.core.command.view.LocalVarsView;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.ThreadLocalWatch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.taobao.arthas.core.advisor.AccessPoint.*;

/**
 * @author beiwei30 on 29/11/2016.
 */
public class LocalVarsAdviceListener extends AdviceListenerAdapter implements LocalVarTraceable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTraceAdviceListener.class);


    private final ThreadLocal<LocalVarsModel> threadBoundEntity = new ThreadLocal<>();
    private final ThreadLocalWatch threadLocalWatch = new ThreadLocalWatch();
    protected LocalVarsCommand command;
    protected CommandProcess process;



    @Override
    final  public void perLine(Class<?> clazz, Object target, String methodName, String methodDesc, Object[] localVars, String[] localNames, Integer line) {
        everyLine(clazz.getClassLoader(), clazz, new ArthasMethod(clazz,methodName,methodDesc), target,localVars, localNames,line  );
    }

    /**
     * Constructor
     */
    public LocalVarsAdviceListener(LocalVarsCommand command, CommandProcess process, boolean verbose) {
         this.command=command;
         this.process=process;
        super.setVerbose(verbose);
    }

    protected LocalVarsModel threadLocalLocalVarsModel() {
        LocalVarsModel localVarsModel = threadBoundEntity.get();
        if (localVarsModel == null) {
            localVarsModel = new LocalVarsModel();
            threadBoundEntity.set(localVarsModel);
        }
        return localVarsModel;
    }

    @Override
    public void destroy() {
        threadBoundEntity.remove();
    }

    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args) throws Throwable {
        Advice advice = Advice.newForBefore(loader, clazz, method, target, args);
        threadBoundEntity.remove();
        threadLocalWatch.start();
        watching(advice);
    }

    public void everyLine(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] localVars, String[] localNames, Integer line) {
       if(line<command.getStartLine()){
           //还没有结果时则反应出去
           return;
       }
        Advice advice = Advice.newForLine(loader, clazz, method, target);
        LocalVarsModel localVarsModel = threadLocalLocalVarsModel();
        Map<String,Object> varMap=new LinkedHashMap<>();
        for (int i = 0; i <localNames.length; i++) {
            varMap.put(localNames[i],localVars[i]);
        }
        if(!StringUtils.isBlank(command.getLocalVarsExpress())){
            try {
             Object v=getExpressionResult(command.getLocalVarsExpress(), varMap, 0);
              localVarsModel.addLine(line, v);
            }catch (ExpressException e) {
                localVarsModel.addLine(line, varMap);
            }
        }else{
            localVarsModel.addLine(line, varMap);
        }

        watching(advice);
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args, Object returnObject) throws Throwable {
        Advice advice = Advice.newForAfterReturning(loader, clazz, method, target, args,returnObject);
        finish(advice);

    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args, Throwable throwable) throws Throwable {
        Advice advice = Advice.newForAfterThrowing(loader, clazz, method, target, args,throwable);
        finish(advice);
    }

    private void finish(Advice advice){
        try {
            double cost = threadLocalWatch.costInMillis();
            boolean conditionResult = isConditionMet(command.getConditionExpress(), advice, cost);
            if (this.isVerbose()) {
                process.write("Condition express: " + command.getConditionExpress() + " , result: " + conditionResult + "\n");
            }
            if(conditionResult){
                LocalVarsModel model = threadLocalLocalVarsModel();
                model.setSizeLimit(command.getSizeLimit());
                model.setExpand(command.getExpand());
                model.setClassName(advice.getClazz().getName());
                model.setMethodName(advice.getMethod().getName());
                if(advice.isAfterReturning()){
                    // 满足输出条件
                    process.times().incrementAndGet();
                    model.setReturnObject(advice.getReturnObj());
                    model.setAccessPoint(ACCESS_AFTER_RETUNING.getValue());
                    LocalVarsView.convertToLine(model);
                    process.appendResult(model);
                    if (isLimitExceeded(command.getNumberOfLimit(), process.times().get())) {
                        // TODO: concurrency issue to abort process
                        abortProcess(process, command.getNumberOfLimit());
                    }
                } else if (advice.isAfterThrowing()) {
                    // 满足输出条件
                    process.times().incrementAndGet();
                    model.setThrowable(advice.getThrowExp());
                    model.setAccessPoint(ACCESS_AFTER_THROWING.getValue());
                    LocalVarsView.convertToLine(model);
                    process.appendResult(model);
                    if (isLimitExceeded(command.getNumberOfLimit(), process.times().get())) {
                        // TODO: concurrency issue to abort process
                        abortProcess(process, command.getNumberOfLimit());
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("localvars failed.", e);
            process.end(1, "localvars failed, , " + e.getMessage()
                    + ", visit " + LogUtil.loggingFile() + " for more details.");
        }finally {
            threadBoundEntity.remove();

        }
    }

    private void watching(Advice advice) {
        try {
            LocalVarsModel model = threadLocalLocalVarsModel();
            model.setSizeLimit(command.getSizeLimit());
            model.setExpand(command.getExpand());
            model.setClassName(advice.getClazz().getName());
            model.setMethodName(advice.getMethod().getName());
            if(advice.isBefore()){
                model.setAccessPoint(ACCESS_BEFORE.getValue());
                model.setShowValue(new StringBuffer());
                model.setMethodArgs(advice.getParams());
                LocalVarsView.convertToLine(model);
            }else if(advice.isLine()){
                AtomicInteger totalNums = model.getTotalNums();
                model.setAccessPoint(ACCESS_LINE.getValue());
                LocalVarsView.convertToLine(model);
                // 是否到达数量限制
                if (isLimitExceeded(command.getLineOfLimit(), totalNums.get())) {
                    process.appendResult(model);
                    threadBoundEntity.remove();
                    process.write("The maximum number of lines to be collected has been reached line-limits:"+command.getLineOfLimit()+", so command will start collecting again . You can set it with -ml option.\n");
                    process.end();

                }
            }
        } catch (Throwable e) {
            logger.warn("localvars failed.", e);
            process.end(1, "localvars failed, , " + e.getMessage()
                    + ", visit " + LogUtil.loggingFile() + " for more details.");
        }
    }
}
