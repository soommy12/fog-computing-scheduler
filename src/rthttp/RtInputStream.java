package rthttp;

import crypt.rot13InputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Bartosz on 15.01.2018.
 */
abstract public class RtInputStream extends InputStream {
    InputStream in;
    OutputStream talkBack;
    abstract void set (InputStream in, OutputStream talkBack);
}

class rotRtInputStream extends RtInputStream {

    public void set(InputStream in, OutputStream talkBack){
        this.in = new rot13InputStream(in);
//        this.in = new ByteArrayInputStream(in.toString().getBytes());
    }
    public int read() throws IOException {
        if (in == null){
            throw new IOException("No Stream");
        }
        return in.read();
    }
}
