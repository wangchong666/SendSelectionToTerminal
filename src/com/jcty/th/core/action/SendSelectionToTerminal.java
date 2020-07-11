package com.jcty.th.core.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
//import com.jediterm.terminal.TerminalOutputStream;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
//import org.jetbrains.plugins.terminal.JBTabbedTerminalWidget;
//import org.jetbrains.plugins.terminal.JBTerminalPanel;
//import org.jetbrains.plugins.terminal.JBTerminalWidget;

/**
 * Created by godlike on 2020/7/10.
 */
public class SendSelectionToTerminal extends AnAction {
    private static final Logger log = Logger.getInstance(SendSelectionToTerminal.class);

//    JBTerminalPanel jbTerminalPanel;
    Object jbTerminalWidget;
    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String selectedText = editor.getSelectionModel().getSelectedText();
        if(selectedText == null|| selectedText.trim().length() == 0){
            editor.getSelectionModel().selectLineAtCaret();
            selectedText = editor.getSelectionModel().getSelectedText().trim();
            editor.getSelectionModel().setSelection(0,0);
        }
//        Messages.showMessageDialog(selectedText, "Information", Messages.getInformationIcon());
//        log.info(selectedText);
        ToolWindow terminal = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Terminal");
        JComponent root = terminal.getComponent();
        if(terminal.isActive()){
            sendText(root,selectedText);
        }
        else{
            String finalSelectedText = selectedText;
            terminal.show(() -> {
                sendText(root, finalSelectedText);
            });
        }

    }

    void sendText(JComponent root,String text){
        printComponent(root);
        if(jbTerminalWidget != null){
            try {
                log.info("invoke");
                Method getTerminal = jbTerminalWidget.getClass().getMethod("getTerminal");
                Object jediTerminal = getTerminal.invoke(jbTerminalWidget);
//                Field[] fields = jediTerminal.getClass().getDeclaredFields();
//                Arrays.stream(fields).forEach(x->log.info(x.getName()+" "+x.getType()));
                Field myTerminalOutputField = jediTerminal.getClass().getDeclaredField("myTerminalOutput");
                myTerminalOutputField.setAccessible(true);
                Object stream =  myTerminalOutputField.get(jediTerminal);
                Method sendString = stream.getClass().getMethod("sendString",String.class);
                Method sendBytes = stream.getClass().getMethod("sendBytes",byte[].class);
                sendString.invoke(stream,text);
                sendBytes.invoke(stream,new byte[]{13});
            } catch (Exception e1) {
                log.error(e1.toString());
            }
        }
    }

    void printComponent(Component c){
        if(c == null) return;
        log.info(c.getName()+" "+c.getClass()+" "+c.getClass().getClassLoader());
        if(c instanceof JPanel){
            printComponent(((JPanel)c).getComponent(0));
        }
        if("terminal".equals(c.getName())){
            log.info("found a JBTerminalWidget");
            jbTerminalWidget = c;
        }
//        if(c instanceof  JBTerminalPanel){
//            log.info("found a JBTerminalPanel");
//            jbTerminalPanel = (JBTerminalPanel) c;
//        }
//        if(c instanceof JBTabbedTerminalWidget){
//            log.info("found a JBTabbedTerminalWidget");
////            jbTerminalWidget = (JBTabbedTerminalWidget) c;
//        }
    }
}
