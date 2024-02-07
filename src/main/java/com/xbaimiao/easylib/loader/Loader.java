package com.xbaimiao.easylib.loader;

import org.bukkit.Bukkit;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

@SuppressWarnings("ALL")
public class Loader extends URLClassLoader {

    static Lookup lookup;
    static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable ignored) {
        }

    }

    public Loader(URL[] urls) {
        super(urls);
    }

    public static boolean addPath(File file) {
        try {
            ClassLoader loader = Bukkit.class.getClassLoader();
            if (loader.getClass().getSimpleName().equals("LaunchClassLoader")) {
                MethodHandle methodHandle = lookup.findVirtual(loader.getClass(), "addURL", MethodType.methodType(Void.TYPE, URL.class));
                methodHandle.invoke(loader, file.toURI().toURL());
            } else {
                Field ucpField;
                try {
                    ucpField = loader.getClass().getDeclaredField("ucp");
                } catch (NoSuchFieldException | NoSuchFieldError var7) {
                    ucpField = loader.getClass().getSuperclass().getDeclaredField("ucp");
                }

                long ucpOffset = unsafe.objectFieldOffset(ucpField);
                Object ucp = unsafe.getObject(loader, ucpOffset);
                MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(Void.TYPE, URL.class));
                methodHandle.invoke(ucp, file.toURI().toURL());
            }

            return true;
        } catch (Throwable var8) {
            var8.printStackTrace();
            return false;
        }
    }

}
