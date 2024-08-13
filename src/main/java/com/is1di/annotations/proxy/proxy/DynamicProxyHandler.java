package com.is1di.annotations.proxy.proxy;

import com.is1di.annotations.Auth;
import com.is1di.annotations.ClassFinder;
import com.is1di.annotations.EntityId;
import com.is1di.annotations.Log;
import com.is1di.exceptions.MessageBusFault;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Logger;

public class DynamicProxyHandler implements InvocationHandler {

    private final Object target;

    private final Map<String, List<Class<? extends MessageBusFault>>> faults;
    private final Logger logger;

    public DynamicProxyHandler(Object target, String exceptionsBasePackage) {
        faults = new HashMap<>();
        this.target = target;
        logger = Logger.getLogger(target.getClass().getName());
        scanForExceptions(exceptionsBasePackage);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        System.out.println("method " + method.getName() + " proxy " + proxy.getClass().getSimpleName() + " args " + Arrays.toString(args));
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (targetMethod.isAnnotationPresent(Auth.class)) {
            Auth authAnnotation = targetMethod.getAnnotation(Auth.class);
            processAuthAnnotation(authAnnotation, method, args);
        }
        boolean logEnd = false;
        boolean logStart = false;
        if (targetMethod.isAnnotationPresent(Log.class)) {
            Log logAnnotation = targetMethod.getAnnotation(Log.class);
            logStart = logAnnotation.start();
            logEnd = logAnnotation.end();
        }
        if (logStart) {
            logger.info("start method " + method.getName());
        }
        result = method.invoke(target, args);
        if (logEnd) {
            logger.info("end method " + method.getName());
        }
        return result;
    }

    private void processAuthAnnotation(Auth authAnnotation, Method method, Object[] args) throws Throwable {
        String op = authAnnotation.value();
        List<Class<? extends MessageBusFault>> classes = faults.get(method.getName());
        int[] errors = authAnnotation.errors();
        List<Class<? extends MessageBusFault>> foundClasses = new ArrayList<>();
        for (int error : errors) {
            classes.stream()
                    .filter(it -> it.getSimpleName()
                            .endsWith("Fault" + error + "Exception"))
                    .findFirst()
                    .ifPresent(foundClasses::add);
        }
        StringBuilder builder = new StringBuilder();
        for (Class<? extends MessageBusFault> clazz : foundClasses) {
            builder.append(clazz.getSimpleName()).append(" ");
        }
        Object entityId = null;
        Parameter[] params = target.getClass().getMethod(method.getName(), method.getParameterTypes()).getParameters();
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(EntityId.class)) {
                entityId = args[i];
            }
        }

        logger.info(" processing auth with params " + "errors: " + builder + " " + "value: " + authAnnotation.value() + " entityId: " + entityId);
    }

    private void scanForExceptions(String basePackage) {
        List<Class<? extends MessageBusFault>> messageBusExceptions = ClassFinder.findAllClassesUsingClassLoader(basePackage, MessageBusFault.class);
        for (Method method : target.getClass().getMethods()) {
            String className = toSimpleClassName(method.getName());
            for (int i = 0; i < messageBusExceptions.size(); i++) {
                if (messageBusExceptions.get(i).getSimpleName().matches("^" + className + "Fault\\d+Exception$")) {
                    var exceptionClazz = messageBusExceptions.get(i);
                    List<Class<? extends MessageBusFault>> exceptList;
                    if (!faults.containsKey(method.getName())) {
                        exceptList = new ArrayList<>();
                        faults.put(method.getName(), exceptList);
                    } else {
                        exceptList = faults.get(method.getName());
                    }
                    exceptList.add(exceptionClazz);
                }
            }
        }
    }

    private String toSimpleClassName(String methodName) {
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
    }
}
