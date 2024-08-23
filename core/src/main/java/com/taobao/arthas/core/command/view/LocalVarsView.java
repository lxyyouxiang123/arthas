package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.advisor.AccessPoint;
import com.taobao.arthas.core.command.model.LocalVarsModel;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.Ansi;
import com.taobao.arthas.core.view.ObjectView;

import java.util.Map;
import java.util.Objects;

/**
 * Term view for LocalVarsModel
 *
 * @author gongdewei 2020/3/27
 */
public class LocalVarsView extends ResultView<LocalVarsModel> {

    @Override
    public void draw(CommandProcess process, LocalVarsModel model) {
          process.write(model.getShowValue().toString());
    }

    public static void convertToLine(LocalVarsModel model){
        StringBuffer buffer=model.getShowValue();
        ObjectVO objectVO = null;
        int accessPoint = model.getAccessPoint();
        final Ansi titleHighlighted = Ansi.ansi().bold().a(Ansi.Attribute.INTENSITY_BOLD).fg(Ansi.Color.CYAN);
        if(accessPoint== AccessPoint.ACCESS_BEFORE.getValue()){
            buffer.append(Ansi.ansi().bold().a(Ansi.Attribute.INTENSITY_BOLD).fg(Ansi.Color.RED).a("method").reset().toString()+"=" + model.getClassName() + "." + model.getMethodName() + "\n");
            objectVO=new ObjectVO(model.getMethodArgs(),model.getExpand());
            String result=StringUtils.objectToString(
                    objectVO.needExpand() ? new ObjectView(model.getSizeLimit(), objectVO).draw() : objectVO.getObject());
            buffer.append(titleHighlighted.a("param==>").reset().toString() + result+ "\n");
        }else if(accessPoint==AccessPoint.ACCESS_LINE.getValue()){
            String result=titleHighlighted.a("line:"+model.getLine()+"==>").reset().toString() ;
            buffer.append(result);
            if(Objects.nonNull(model.getLocalVars())){
                Object localVars = model.getLocalVars();
                if(localVars instanceof Map<?,?>){
                    Map<?,?> v= (Map<?, ?>) localVars;
                    v.forEach((varName, varValue)->{
                        ObjectVO  oVO=new ObjectVO(varValue,model.getExpand());
                        final Ansi varHighlighted = Ansi.ansi().fg(Ansi.Color.GREEN);
                        buffer.append(varHighlighted.a(varName.toString()).reset().toString()+"="+StringUtils.objectToString(
                                oVO.needExpand() ? new ObjectView(model.getSizeLimit(), oVO).draw() : oVO.getObject())+",");
                    });
                }else{
                    ObjectVO  oVO=new ObjectVO(localVars,model.getExpand());
                    buffer.append(StringUtils.objectToString(
                            oVO.needExpand() ? new ObjectView(model.getSizeLimit(), oVO).draw() : oVO.getObject()));
                }

            }
            buffer.append("\n");
        }else if(accessPoint==AccessPoint.ACCESS_AFTER_THROWING.getValue()){
            objectVO=new ObjectVO(model.getThrowable(),model.getExpand());
            String result=StringUtils.objectToString(
                    objectVO.needExpand() ? new ObjectView(model.getSizeLimit(), objectVO).draw() : objectVO.getObject());
            buffer.append(titleHighlighted.a("throwExp==>").reset().toString()  + result+ "\n");
        }else if(accessPoint==AccessPoint.ACCESS_AFTER_RETUNING.getValue()){
            objectVO=new ObjectVO(model.getReturnObject(),model.getExpand());
            String result=StringUtils.objectToString(
                    objectVO.needExpand() ? new ObjectView(model.getSizeLimit(), objectVO).draw() : objectVO.getObject());
            buffer.append(titleHighlighted.a("returnObject==>").reset().toString()  + result+ "\n");
        }
    }
}
