package com.ruoyi.web.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Canvas Editor 集成测试
 * 验证服务初始化和数据库连接
 * 注意：此测试需要 Redis 和数据库等外部基础设施，
 * 在普通打包环境下会因无法连接 Redis 而失败，
 * 因此默认禁用。可在有完整基础设施的环境下手动启用。
 */
@Disabled("需要 Redis 和数据库等外部基础设施，打包时跳过")
@SpringBootTest(classes = com.ruoyi.RuoYiApplication.class)
public class CanvasEditorIntegrationTest {

    @Autowired
    private CanvasEditorService canvasEditorService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private org.springframework.core.env.Environment environment;

    @Test
    void testServiceInitialization() {
        assertNotNull(canvasEditorService, "CanvasEditorService应该被正确初始化");
        assertNotNull(jdbcTemplate, "JdbcTemplate应该被正确注入");
        assertNotNull(dataSource, "DataSource应该被正确注入");
    }

    @Test
    void testDatabaseConnection() throws SQLException {
        // 测试数据库连接
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应该为null");
            assertFalse(connection.isClosed(), "数据库连接不应该关闭");

            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData, "数据库元数据不应该为null");
            System.out.println("数据库产品: " + metaData.getDatabaseProductName());
            System.out.println("数据库版本: " + metaData.getDatabaseProductVersion());
        } catch (SQLException e) {
            fail("数据库连接失败: " + e.getMessage());
        }
    }

    @Test
    void testCanvasEditHistoryTableExists() throws SQLException {
        // 检查canvas_edit_history表是否存在
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "canvas_edit_history", null);

            assertTrue(tables.next(), "canvas_edit_history表应该存在");
            assertEquals("TABLE", tables.getString("TABLE_TYPE"), "应该是表类型");

            System.out.println("✅ canvas_edit_history表存在");
        } catch (SQLException e) {
            fail("查询表信息失败: " + e.getMessage());
        }
    }

    @Test
    void testEditHistoryTableStructure() {
        // 验证表结构
        try {
            // 插入测试数据
            String sql = "INSERT INTO canvas_edit_history " +
                    "(image_id, user_id, operation, params, result_url, version, create_time) " +
                    "VALUES ('test-image-001', 1, 'extend', '{\"ratio\":\"16:9\"}', 'http://test.com/result.png', 1, NOW())";

            jdbcTemplate.execute(sql);

            // 查询数据
            String querySql = "SELECT * FROM canvas_edit_history WHERE image_id = 'test-image-001' ORDER BY id DESC LIMIT 1";
            Map<String, Object> result = jdbcTemplate.queryForMap(querySql);

            assertNotNull(result, "查询结果不应该为null");
            assertEquals("test-image-001", result.get("image_id"), "image_id应该匹配");
            assertEquals("extend", result.get("operation"), "operation应该匹配");
            assertEquals(1, result.get("version"), "version应该匹配");

            System.out.println("✅ 编辑历史表结构验证通过");
            System.out.println("测试数据: " + result);

            // 清理测试数据
            jdbcTemplate.execute("DELETE FROM canvas_edit_history WHERE image_id = 'test-image-001'");

        } catch (Exception e) {
            fail("表结构验证失败: " + e.getMessage());
        }
    }

    @Test
    void testGetNextVersion() {
        try {
            // 清理测试数据
            jdbcTemplate.execute("DELETE FROM canvas_edit_history WHERE image_id = 'version-test-001'");

            // 测试1: 新图片，版本号应该从1开始
            Integer version1 = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(version), 0) + 1 FROM canvas_edit_history WHERE image_id = ?",
                    Integer.class, "version-test-001");
            assertEquals(1, version1, "新图片版本号应该从1开始");

            // 插入一条记录
            jdbcTemplate.execute("INSERT INTO canvas_edit_history (image_id, user_id, operation, params, result_url, version, create_time) " +
                    "VALUES ('version-test-001', 1, 'extend', '{}', 'http://test.com/1.png', 1, NOW())");

            // 测试2: 获取下一个版本号
            Integer version2 = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(version), 0) + 1 FROM canvas_edit_history WHERE image_id = ?",
                    Integer.class, "version-test-001");
            assertEquals(2, version2, "下一个版本号应该是2");

            // 测试3: 插入第二版
            jdbcTemplate.execute("INSERT INTO canvas_edit_history (image_id, user_id, operation, params, result_url, version, create_time) " +
                    "VALUES ('version-test-001', 1, 'extend', '{}', 'http://test.com/2.png', 2, NOW())");

            // 测试4: 获取下一个版本号
            Integer version3 = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(version), 0) + 1 FROM canvas_edit_history WHERE image_id = ?",
                    Integer.class, "version-test-001");
            assertEquals(3, version3, "下一个版本号应该是3");

            // 清理测试数据
            jdbcTemplate.execute("DELETE FROM canvas_edit_history WHERE image_id = 'version-test-001'");

            System.out.println("✅ 版本号管理测试通过");

        } catch (Exception e) {
            fail("版本号测试失败: " + e.getMessage());
        }
    }

    @Test
    void testVertexAiConfiguration() {
        // 测试Vertex AI配置是否正确加载
        // 注意: 这个测试需要application.yml中有正确的配置

        String projectId = environment.getProperty("vertex.ai.project-id");
        String location = environment.getProperty("vertex.ai.location", "global");
        String model = environment.getProperty("vertex.ai.model", "gemini-3-pro-image");

        System.out.println("Vertex AI配置:");
        System.out.println("  Project ID: " + (projectId != null ? "已配置" : "未配置"));
        System.out.println("  Location: " + location);
        System.out.println("  Model: " + model);

        // 至少要有Project ID
        assertNotNull(projectId, "Vertex AI Project ID应该被配置");
    }

    @Test
    void testUploadPathConfiguration() {
        // 测试上传路径配置
        String uploadPath = environment.getProperty("ruoyi.profile", "/tmp/uploadPath");
        String uploadType = environment.getProperty("customer.image-upload.type", "LOCAL");

        System.out.println("图片上传配置:");
        System.out.println("  上传路径: " + uploadPath);
        System.out.println("  上传类型: " + uploadType);

        assertNotNull(uploadPath, "上传路径应该被配置");
        assertNotNull(uploadType, "上传类型应该被配置");
    }
}
