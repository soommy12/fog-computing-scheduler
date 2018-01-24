package crypt;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bartosz on 15.01.2018.
 */
public class rot13InputStream extends FilterInputStream{

    public rot13InputStream(InputStream i) {
        super(i);
    }

    public int read() throws IOException{
        return rot13( in.read());
    }

    private int rot13(int c) {
        if( ( c >= 'A') && (c <= 'Z')) c = (((c-'A')+13)%26)+'A'; if( ( c >= 'a') && (c <= 'z'))
            c = (((c-'a')+13)%26)+'a';
        return c;
    }
}
