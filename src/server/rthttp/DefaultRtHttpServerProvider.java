package server.rthttp;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Bartosz on 16.01.2018.
 */
public class DefaultRtHttpServerProvider extends RtHttpServerProvider {
    public DefaultRtHttpServerProvider() {
    }

    public RtHttpServer createRtHttpServer(InetSocketAddress var1, int var2) throws IOException {
        return new RtHttpServerImpl(var1, var2);
    }

}
