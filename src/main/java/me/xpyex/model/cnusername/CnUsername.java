package me.xpyex.model.cnusername;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class CnUsername {
    public static final String CLASS_PATH_LOGIN_SPIGOT = "net/minecraft/server/network/LoginListener";
    public static final String CLASS_PATH_LOGIN_MCP = "net/minecraft/server/network/ServerLoginPacketListenerImpl";
    public static final String CLASS_PATH_LOGIN_YARN = "net/minecraft/server/network/ServerLoginNetworkHandler";
    public static final String CLASS_PATH_STRING = "com/mojang/brigadier/StringReader";

    public static void premain(String agentArgs, Instrumentation inst) {
        Logging.info("开始载入模块 CnUsername");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");
        Logging.info("等待Minecraft加载...");

        String arg;
        if (agentArgs == null || agentArgs.isEmpty()) {
            arg = "^[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}$";
        } else {
            try {
                Pattern.compile(agentArgs);
                arg = agentArgs;
            } catch (PatternSyntaxException e) {
                Logging.warning("你自定义的正则格式错误: " + agentArgs);
                e.printStackTrace();
                arg = "^[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}$";
            }
        }
        String finalArg = arg;
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
                switch (className) {
                    case CLASS_PATH_LOGIN_MCP:
                    case CLASS_PATH_LOGIN_SPIGOT:
                    case CLASS_PATH_LOGIN_YARN:
                        Logging.info("开始修改类 " + className);
                        try {
                            ClassReader classReader = new ClassReader(classFileBuffer);
                            ClassWriter classWriter = new ClassWriter(classReader, 0);
                            ClassVisitor classVisitor = new ClassVisitorLoginListener(className, classWriter, finalArg);
                            classReader.accept(classVisitor, 0);
                            Logging.info("修改完成并保存");
                            return classWriter.toByteArray();
                            // 加载类
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logging.warning("修改失败: " + e);
                        }
                        break;
                    case CLASS_PATH_STRING:
                        Logging.info("开始修改类 " + className);
                        try {
                            ClassReader classReader = new ClassReader(classFileBuffer);
                            ClassWriter classWriter = new ClassWriter(classReader, 0);
                            ClassVisitor classVisitor = new ClassVisitorStringReader(className, classWriter);
                            classReader.accept(classVisitor, 0);
                            Logging.info("修改完成并保存");
                            return classWriter.toByteArray();
                            // 加载类
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logging.warning("修改失败: " + e);
                        }
                        break;
                    case "org/bukkit/plugin/EventExecutor$1":
                        try {
                            Logging.setLogger((Logger) Class.forName("org.bukkit.Bukkit", false, loader).getMethod("getLogger").invoke(null));
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return null;
            }
        });
    }
}