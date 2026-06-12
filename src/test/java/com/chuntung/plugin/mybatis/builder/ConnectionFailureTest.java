package com.chuntung.plugin.mybatis.builder;

import com.chuntung.plugin.mybatis.builder.action.SettingsPresenter;
import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DriverTypeEnum;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

public class ConnectionFailureTest extends BasePlatformTestCase {

    @Test
    public void testUnknownHostExceptionHandling() {
        MybatisBuilderService service = new MybatisBuilderService(getProject());
        ConnectionInfo info = new ConnectionInfo();
        info.setName("Neon Test");
        info.setDriverType(DriverTypeEnum.PostgreSQL);
        // Using a non-existent host to trigger UnknownHostException
        info.setHost("ep-icy-sunset-a1209kns-pooler.ap-southeast-1.aws.neon.tech.invalid");
        info.setPort(5432);
        info.setDatabase("neondb");
        info.setUserName("user");
        info.setPassword("pass");

        try {
            SettingsPresenter handler = new SettingsPresenter(getProject());
            handler.testConnection(info);
            // In headless environment, Messages.showErrorDialog might throw an exception
            // containing the message if not mocked properly, or just log it.
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Caught exception from testConnection: " + e.getClass().getName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("[DEBUG_LOG] Cause: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
            // If it's a headless exception, it often contains the message
            assertTrue(e.getMessage().contains("Unknown host"));
        }
    }
}
