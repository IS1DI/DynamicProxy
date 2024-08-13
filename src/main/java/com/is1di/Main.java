package com.is1di;

import com.is1di.annotations.ClassFinder;
import com.is1di.annotations.proxy.proxy.DynamicProxyHandler;
import com.is1di.exceptions.MessageBusFault;

import java.lang.reflect.Proxy;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        var classes = ClassFinder.findAllClassesUsingClassLoader("com.is1di", MessageBusFault.class);
        Test proxyTest = (Test) Proxy.newProxyInstance(TestImpl.class.getClassLoader(), new Class[]{Test.class}, new DynamicProxyHandler(new TestImpl(), "com.is1di"));
        proxyTest.test2(123L, "abs", "asd");
        proxyTest.test();
    }
}