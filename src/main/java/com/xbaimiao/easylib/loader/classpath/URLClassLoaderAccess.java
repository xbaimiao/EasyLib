/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.xbaimiao.easylib.loader.classpath;

import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * Provides access to {@link URLClassLoader}#addURL.
 */
public abstract class URLClassLoaderAccess {

    private final URLClassLoader classLoader;

    protected URLClassLoaderAccess(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Creates a {@link URLClassLoaderAccess} for the given class loader.
     *
     * @param classLoader the class loader
     * @return the access object
     */
    public static URLClassLoaderAccess create(URLClassLoader classLoader) {
        if (Reflection.isSupported()) {
            return new Reflection(classLoader);
        } else if (Unsafe.isSupported()) {
            return new Unsafe(classLoader);
        } else if (GlobalUnsafe.isSupported()) {
            return new GlobalUnsafe(classLoader);
        } else {
            return Noop.INSTANCE;
        }
    }

    private static void throwError(Throwable cause) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("unable to inject into the plugin URLClassLoader.\n" +
                "You may be able to fix this problem by adding the following command-line argument " +
                "directly after the 'java' command in your start script: \n'--add-opens java.base/java.lang=ALL-UNNAMED'", cause);
    }

    /**
     * Adds the given URL to the class loader.
     *
     * @param url the URL to add
     */
    public abstract void addURL(URL url);

    /**
     * Accesses using reflection, not supported on Java 9+.
     */
    private static class Reflection extends URLClassLoaderAccess {
        private static final Method ADD_URL_METHOD;

        static {
            Method addUrlMethod;
            try {
                addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
            } catch (Exception e) {
                addUrlMethod = null;
            }
            ADD_URL_METHOD = addUrlMethod;
        }

        Reflection(URLClassLoader classLoader) {
            super(classLoader);
        }

        private static boolean isSupported() {
            return ADD_URL_METHOD != null;
        }

        @Override
        public void addURL(URL url) {
            try {
                ADD_URL_METHOD.invoke(super.classLoader, url);
            } catch (ReflectiveOperationException e) {
                URLClassLoaderAccess.throwError(e);
            }
        }
    }

    /**
     * Accesses using sun.misc.Unsafe, supported on Java 9+.
     *
     * @author Vaishnav Anil (https://github.com/slimjar/slimjar)
     */
    private static class Unsafe extends URLClassLoaderAccess {
        private static final sun.misc.Unsafe UNSAFE;

        static {
            sun.misc.Unsafe unsafe;
            try {
                Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            } catch (Throwable t) {
                unsafe = null;
            }
            UNSAFE = unsafe;
        }

        private final Collection<URL> unopenedURLs;
        private final Collection<URL> pathURLs;
        @SuppressWarnings("unchecked")
        Unsafe(URLClassLoader classLoader) {
            super(classLoader);

            Collection<URL> unopenedURLs;
            Collection<URL> pathURLs;
            try {
                Object ucp = fetchField(URLClassLoader.class, classLoader, "ucp");
                unopenedURLs = (Collection<URL>) fetchField(ucp.getClass(), ucp, "unopenedUrls");
                pathURLs = (Collection<URL>) fetchField(ucp.getClass(), ucp, "path");
            } catch (Throwable e) {
                unopenedURLs = null;
                pathURLs = null;
            }

            this.unopenedURLs = unopenedURLs;
            this.pathURLs = pathURLs;
        }

        private static boolean isSupported() {
            return UNSAFE != null;
        }

        private static Object fetchField(final Class<?> clazz, final Object object, final String name) throws NoSuchFieldException {
            Field field = clazz.getDeclaredField(name);
            long offset = UNSAFE.objectFieldOffset(field);
            return UNSAFE.getObject(object, offset);
        }

        @Override
        public void addURL(URL url) {
            if (this.unopenedURLs == null || this.pathURLs == null) {
                URLClassLoaderAccess.throwError(new NullPointerException("unopenedURLs or pathURLs"));
            }

            synchronized (this.unopenedURLs) {
                this.unopenedURLs.add(url);
                this.pathURLs.add(url);
            }
        }
    }

    private static class GlobalUnsafe extends URLClassLoaderAccess {

        static MethodHandles.Lookup lookup;
        static sun.misc.Unsafe unsafe;

        static {
            try {
                Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                unsafe = (sun.misc.Unsafe) field.get(null);
                Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                Object lookupBase = unsafe.staticFieldBase(lookupField);
                long lookupOffset = unsafe.staticFieldOffset(lookupField);
                lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
            } catch (Throwable ignored) {
                lookup = null;
            }

        }

        protected GlobalUnsafe(URLClassLoader classLoader) {
            super(classLoader);
        }

        private static boolean isSupported() {
            return lookup != null;
        }

        @Override
        public void addURL(URL url) {
            try {
                ClassLoader loader = Bukkit.class.getClassLoader();
                if (loader.getClass().getSimpleName().equals("LaunchClassLoader")) {
                    MethodHandle methodHandle = lookup.findVirtual(loader.getClass(), "addURL", MethodType.methodType(Void.TYPE, URL.class));
                    methodHandle.invoke(loader, url.toURI().toURL());
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
                    methodHandle.invoke(ucp, url.toURI().toURL());
                }
            } catch (Throwable var8) {
                throw new RuntimeException(var8);
            }
        }
    }

    private static class Noop extends URLClassLoaderAccess {
        private static final Noop INSTANCE = new Noop();

        private Noop() {
            super(null);
        }

        @Override
        public void addURL(URL url) {
            URLClassLoaderAccess.throwError(null);
        }
    }

}
