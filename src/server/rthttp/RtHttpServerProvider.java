package server.rthttp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Created by Bartosz on 16.01.2018.
 */
public class RtHttpServerProvider extends HttpServerProvider {

    private static final Object lock = new Object();
    private static RtHttpServerProvider provider = null;

    protected RtHttpServerProvider() {
        SecurityManager var1 = System.getSecurityManager();
        if(var1 != null) {
            var1.checkPermission(new RuntimePermission("httpServerProvider"));
        }

    }

    private static boolean loadProviderFromProperty() {
        String var0 = System.getProperty("com.sun.net.httpserver.HttpServerProvider");
        if(var0 == null) {
            return false;
        } else {
            try {
                Class var1 = Class.forName(var0, true, ClassLoader.getSystemClassLoader());
                provider = (RtHttpServerProvider)var1.newInstance();
                return true;
            } catch (IllegalAccessException | InstantiationException | SecurityException | ClassNotFoundException var2) {
                throw new ServiceConfigurationError((String)null, var2);
            }
        }
    }

    private static boolean loadProviderAsService() {
        Iterator var0 = ServiceLoader.load(HttpServerProvider.class, ClassLoader.getSystemClassLoader()).iterator();

        while(true) {
            try {
                if(!var0.hasNext()) {
                    return false;
                }

                provider = (RtHttpServerProvider)var0.next();
                return true;
            } catch (ServiceConfigurationError var2) {
                if(!(var2.getCause() instanceof SecurityException)) {
                    throw var2;
                }
            }
        }
    }

    @Override
    public HttpServer createHttpServer(InetSocketAddress inetSocketAddress, int i) throws IOException {
        return null;
    }

    @Override
    public HttpsServer createHttpsServer(InetSocketAddress inetSocketAddress, int i) throws IOException {
        return null;
    }

    public static HttpServerProvider provider() {
        Object var0 = lock;
        synchronized(lock) {
            return provider != null?provider:(HttpServerProvider)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    if(RtHttpServerProvider.loadProviderFromProperty()) {
                        return RtHttpServerProvider.provider;
                    } else if(RtHttpServerProvider.loadProviderAsService()) {
                        return RtHttpServerProvider.provider;
                    } else {
                        RtHttpServerProvider.provider = new DefaultRtHttpServerProvider();
                        return RtHttpServerProvider.provider;
                    }
                }
            });
        }
    }

    public class provider extends RtHttpServerProvider {
    }
}
