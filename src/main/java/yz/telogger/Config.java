package yz.telogger;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 配置，单例
 *
 * @author zenuo
 * 2018/07/04 13:55
 */
@Slf4j
enum Config {
    INSTANCE;

    /**
     * 端口
     */
    private int port;

    /**
     * 是否启用SSL加密
     */
    private boolean ssl;

    /**
     * 初始化
     */
    void init() {
        final Path teloggerConfPath = Paths.get(Constants.CONF_PATH_TELOGGER);
        if (Files.exists(teloggerConfPath)) {
            final Properties teloggerConf = new Properties();
            try {
                final FileInputStream fis = new FileInputStream(Constants.CONF_PATH_TELOGGER);
                teloggerConf.load(fis);
                fis.close();

                //读取端口
                final String port = teloggerConf.getProperty("port");
                if (port != null) {
                    this.port = Integer.parseInt(port);
                } else {
                    this.port = Constants.DEFAULT_PORT;
                }

                //读取是否启用SSL
                final String ssl = teloggerConf.getProperty("ssl");
                if (ssl != null) {
                    this.ssl = Boolean.parseBoolean(ssl);
                } else {
                    this.ssl = Constants.DEFAULT_SSL_ENABLED;
                }

                log.info("initialized, port={}, ssl={}", this.port, this.ssl);
            } catch (Exception e) {
                log.warn("load conf", e);
            }
        } else {
            log.error("File {} not exists", Constants.CONF_PATH_TELOGGER);
            System.exit(1);
        }
    }

    int getPort() {
        return port;
    }

    boolean isSsl() {
        return ssl;
    }
}
