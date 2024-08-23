package com.taobao.arthas.core;



import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.taobao.arthas.core.config.Configure;
import com.taobao.arthas.core.util.IOUtils;
import org.junit.Test;

import java.io.*;
import java.net.*;
import java.util.Objects;

/**
 * describe:
 *
 * @author lxy
 * @date 2020/11/26
 */
public class ArthasTest {

    String projectRootPath = new File(System.getProperty("user.dir")).getParentFile().toString();

    @Test
    public void test() throws InterruptedException {

        File arthasCore = new File(projectRootPath, "core/target/arthas-core-shade.jar");
        File arthasAgent = new File(projectRootPath, "agent/target/arthas-agent-jar-with-dependencies.jar");
        File arthasDemo = new File(projectRootPath, "math-game/target/math-game.jar");
        File arthasClient = new File(projectRootPath, "client/target/arthas-client-jar-with-dependencies.jar");

        File spyJar = new File(projectRootPath, "spy/target/arthas-spy.jar");
        File coreSpyJar = new File(projectRootPath, "core/target/arthas-spy.jar");
        if (!coreSpyJar.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(spyJar);
                FileOutputStream fileOutputStream = new FileOutputStream(coreSpyJar);
                IOUtils.copyLarge(fileInputStream, fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final String startUpCommand = generateThirdJarStartUpCommand(arthasDemo.getAbsolutePath());
        System.out.println(startUpCommand);

        class ThridJarThread extends Thread {
            Process exec = null;

            @Override
            public void run() {
                try {
                    exec = Runtime.getRuntime().exec(startUpCommand);
                    InputStream inputStream = exec.getInputStream();
                    System.out.println(IOUtils.toString(inputStream));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void destroy0() {
                if (exec!=null) {
                    exec.destroy();
                }
            }
        }

        final ThridJarThread thread = new ThridJarThread();
     //   thread.start();

        Thread.sleep(5000);

        String pid = getPid(arthasDemo.getName());
        System.err.println("pid="+pid);
        if(Objects.nonNull(pid)){
            Arthas.main(new String[]{"-pid", pid,
                    "-core", arthasCore.getAbsolutePath(),
                    "-agent", arthasAgent.getAbsolutePath(),
                    "-target-ip", "127.0.0.1",
                    "-telnet-port", "3658"
            });
        }


//        URLClassLoader classLoader = null;
//        try {
//            classLoader = new URLClassLoader(
//                    new URL[] { arthasClient.toURI().toURL() });
//            Class<?> telnetConsoleClas = classLoader.loadClass("com.taobao.arthas.client.TelnetConsole");
//            Method mainMethod = telnetConsoleClas.getMethod("main", String[].class);
//            Thread.currentThread().setContextClassLoader(classLoader);
//            Arrays.stream(mainMethod.getParameterTypes()).forEach(aClass -> {
//                System.out.println(aClass);
//            });
//            mainMethod.invoke(null,new Object[]{new String[]{"127.0.0.1","3658"}});
//        } catch (MalformedURLException | ClassNotFoundException | NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                thread.destroy0();
                thread.interrupt();

            }
        });

        Thread.currentThread().join();

    }

    public String generateThirdJarStartUpCommand(String thirdJarFilePath) {
        Configure configure = new Configure();
        File arthasCore = new File(projectRootPath, "/core/target/arthas-core-shade.jar");
        File arthasAgent = new File(projectRootPath, "/agent/target/arthas-agent-jar-with-dependencies.jar");
        configure.setArthasCore(encodeArg(arthasCore.getAbsolutePath()));
        configure.setArthasAgent(encodeArg(arthasAgent.getAbsolutePath()));
        configure.setIp("127.0.0.1");
        configure.setTelnetPort(3658);

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("java  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 ")
                .append(" -javaagent:").append(decodeArg(configure.getArthasAgent())).append("=").append(decodeArg(configure.getArthasCore()) + ";" + configure.toString())
                .append("  ")
                .append("-jar ").append(thirdJarFilePath);
        return stringBuffer.toString();
    }

    private static String encodeArg(String arg) {
        try {
            return URLEncoder.encode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }

    private static String decodeArg(String arg) {
        try {
            return URLDecoder.decode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }


    public String getPid(String pidName) {
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            String pid = descriptor.id();
            if (descriptor.displayName().contains(pidName)) {
                return pid;
            }
        }
        return null;
    }


    @Test
    public void  test3() throws MalformedURLException, ClassNotFoundException {
        File arthasCore = new File(projectRootPath, "core/target/arthas-core-shade.jar");
        URLClassLoader n=new URLClassLoader(new URL[]{arthasCore.toURI().toURL()});
        Class<?> aClass = n.loadClass("com.taobao.arthas.core.advisor.SpyInterceptors$SpyLineInterceptor");
        System.out.println(aClass);
    }
}
