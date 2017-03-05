package com.tianxing.userapps.guahao5;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import java.io.File;
import android.os.Environment;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
/**
 * Created by litao on 2017/3/4.
 */

public class ALogger {
    public static org.apache.log4j.Logger getLogger(Class clazz) {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(Environment.getExternalStorageDirectory().toString() + File.separator + "log/guahao.log");
        logConfigurator.setRootLevel(Level.ALL);
        logConfigurator.setLevel("org.apache", Level.ALL);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();
        Logger log = Logger.getLogger(clazz);
        return log;
    }
}