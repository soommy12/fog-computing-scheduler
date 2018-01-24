package server.rthttp;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

/**
 * Created by Bartosz on 16.01.2018.
 */
public class RtHttpServer extends HttpServer {

//    public static RtHttpServer create(InetSocketAddress host, int port) throws IOException {
//        RtHttpServerProvider provider = new RtHttpServerProvider.provider();
//        return provider.createRtHttpServer(host, port);
//    }

    @Override
    public void bind(InetSocketAddress inetSocketAddress, int i) throws IOException {

    }

    @Override
    public void start() {

    }

    @Override
    public void setExecutor(Executor executor) {

    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void stop(int i) {

    }

    @Override
    public HttpContext createContext(String s, HttpHandler httpHandler) {
        return null;
    }

    @Override
    public HttpContext createContext(String s) {
        return null;
    }

    @Override
    public void removeContext(String s) throws IllegalArgumentException {

    }

    @Override
    public void removeContext(HttpContext httpContext) {

    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }
}
