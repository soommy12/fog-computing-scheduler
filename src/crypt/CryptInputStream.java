package crypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Bartosz on 15.01.2018.
 */

//todo refactor
abstract class CryptInputStream extends InputStream {
    InputStream in;
    OutputStream talkBack;
    abstract public void set(InputStream in, OutputStream talkBack);
}

class rot13CryptInputStream extends CryptInputStream {

    public void set (InputStream in, OutputStream talkBack) {
        this.in = new rot13InputStream(in);
    }

    public int read() throws IOException {
        if (in == null)
            throw new IOException("No stream");
        return in.read();
    }
}


