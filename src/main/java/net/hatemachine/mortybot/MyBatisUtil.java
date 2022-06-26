package net.hatemachine.mortybot;

import net.hatemachine.mortybot.config.BotProperties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyBatisUtil {

    private static final Logger log = LoggerFactory.getLogger(MyBatisUtil.class);

    private static SqlSessionFactory sqlSessionFactory;

    static {
        Properties props = BotProperties.getBotProperties().getAll();
        String resource = "mybatis-config.xml";
        try (InputStream is = Resources.getResourceAsStream(resource)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(is, props);
        } catch (IOException e) {
            log.error("Failed to create SQL session factory, resource: {}", resource, e);
        }
    }

    private MyBatisUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}
