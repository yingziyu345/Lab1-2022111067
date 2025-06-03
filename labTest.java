import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import java.lang.reflect.Field;

class labTest {

    @BeforeAll
    static void setUpClass() throws IOException {
        // 使用Easy Test.txt构建图
        String text = "The scientist carefully analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again.";

        // 通过反射访问私有方法buildGraph
        try {
            java.lang.reflect.Method buildGraphMethod = lab.class.getDeclaredMethod("buildGraph", String.class);
            buildGraphMethod.setAccessible(true);
            Object graph = buildGraphMethod.invoke(null, text);

            // 设置静态graph字段
            Field graphField = lab.class.getDeclaredField("graph");
            graphField.setAccessible(true);
            graphField.set(null, graph);
        } catch (Exception e) {
            fail("设置测试图失败: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        // 确保每个测试前图都已正确初始化
        assertNotNull(getGraphField(), "图应该已经初始化");
    }

    // 辅助方法：获取graph字段
    private Object getGraphField() {
        try {
            Field graphField = lab.class.getDeclaredField("graph");
            graphField.setAccessible(true);
            return graphField.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    // 辅助方法：设置graph为null
    private void setGraphToNull() {
        try {
            Field graphField = lab.class.getDeclaredField("graph");
            graphField.setAccessible(true);
            graphField.set(null, null);
        } catch (Exception e) {
            fail("设置graph为null失败");
        }
    }

    // 辅助方法：恢复graph
    private void restoreGraph() {
        try {
            setUpClass();
        } catch (IOException e) {
            fail("恢复graph失败");
        }
    }

    /**
     * 测试用例编号：1
     * 等价类3: 图未初始化
     * 测试当graph为null时的情况
     */
    @Test
    void testQueryBridgeWords_GraphNotInitialized() {
        Object originalGraph = getGraphField();
        try {
            setGraphToNull();
            String result = lab.queryBridgeWords("the", "scientist");
            assertEquals("图未初始化！", result);
            System.out.println("测试1通过");
        } finally {
            restoreGraph();
        }
    }

    /**
     * 测试用例编号：2
     * 等价类4: 第一个单词不在图中
     * 测试word1不存在的情况
     */
    @Test
    void testQueryBridgeWords_Word1NotInGraph() {
        String result = lab.queryBridgeWords("nonexistent", "scientist");
        assertEquals("No nonexistent in the graph!", result);
        System.out.println("测试2通过");
    }

    /**
     * 测试用例编号：3
     * 等价类5: 第二个单词不在图中
     * 测试word2不存在的情况
     */
    @Test
    void testQueryBridgeWords_Word2NotInGraph() {
        String result = lab.queryBridgeWords("the", "nonexistent");
        assertEquals("No nonexistent in the graph!", result);
        System.out.println("测试3通过");
    }

    /**
     * 测试用例编号：4
     * 等价类6: 两个单词都不在图中
     * 测试word1和word2都不存在的情况
     */
    @Test
    void testQueryBridgeWords_BothWordsNotInGraph() {
        String result = lab.queryBridgeWords("nonexistent1", "nonexistent2");
        assertEquals("No nonexistent1 or nonexistent2 in the graph!", result);
        System.out.println("测试4通过");
    }

    /**
     * 测试用例编号：5
     * 等价类7: 两个单词相同
     * 测试word1等于word2的情况
     */
    @Test
    void testQueryBridgeWords_SameWords() {
        String result = lab.queryBridgeWords("the", "the");
        assertEquals("No bridge words from the to the!", result);
        System.out.println("测试5通过");
    }

    /**
     * 测试用例编号：6
     * 等价类7: 两个单词相同（大小写不同）
     * 测试大小写不敏感的相同单词
     */
    @Test
    void testQueryBridgeWords_SameWordsDifferentCase() {
        String result = lab.queryBridgeWords("The", "THE");
        assertEquals("No bridge words from the to the!", result);
        System.out.println("测试6通过");
    }

    /**
     * 测试用例编号：7
     * 等价类1: 存在桥接词的情况
     * 测试从"requested"到"data"，应该存在桥接词"more"
     */
    @Test
    void testQueryBridgeWords_BridgeWordsExist() {
        String result = lab.queryBridgeWords("requested", "data");
        assertTrue(result.startsWith("The bridge words from requested to data are:"));
        assertTrue(result.contains("more"));
        System.out.println("测试7通过");
    }

    /**
     * 测试用例编号：8
     * 等价类8: 不存在桥接词的情况
     * 测试两个没有连接路径的单词
     */
    @Test
    void testQueryBridgeWords_NoBridgeWords() {
        String result = lab.queryBridgeWords("again", "the");
        assertEquals("No bridge words from again to the!", result);
        System.out.println("测试8通过");
    }

    /**
     * 测试用例编号：9
     * 等价类9: 直接相邻的单词（无桥接词）
     * 测试直接相邻的单词，路径长度为2
     */
    @Test
    void testQueryBridgeWords_DirectlyConnected() {
        String result = lab.queryBridgeWords("the", "scientist");
        assertEquals("No bridge words from the to scientist!", result);
        System.out.println("测试9通过");
    }

    /**
     * 测试用例编号：10
     * 等价类2: 大小写混合输入
     * 测试函数对大小写的处理
     */
    @Test
    void testQueryBridgeWords_MixedCase() {
        String result1 = lab.queryBridgeWords("REQUESTED", "data");
        String result2 = lab.queryBridgeWords("requested", "DATA");
        String result3 = lab.queryBridgeWords("Requested", "Data");

        assertTrue(result1.startsWith("The bridge words from requested to data are:"));
        assertTrue(result2.startsWith("The bridge words from requested to data are:"));
        assertTrue(result3.startsWith("The bridge words from requested to data are:"));
        System.out.println("测试10通过");
    }

    /**
     * 测试用例编号：11
     * 等价类10: 空字符串输入
     * 测试空字符串作为输入的情况
     */
    @Test
    void testQueryBridgeWords_EmptyStrings() {
        String result1 = lab.queryBridgeWords("", "scientist");
        String result2 = lab.queryBridgeWords("the", "");
        String result3 = lab.queryBridgeWords("", "");

        assertTrue(result1.contains("in the graph!"));
        assertTrue(result2.contains("in the graph!"));
        assertTrue(result3.contains("in the graph!"));
        System.out.println("测试11通过");
    }

    /**
     * 测试用例编号：12
     * 等价类11: 包含特殊字符的输入
     * 测试包含标点符号等特殊字符的输入
     */
    @Test
    void testQueryBridgeWords_SpecialCharacters() {
        String result = lab.queryBridgeWords("the!", "scientist.");
        assertEquals("No the! or scientist. in the graph!", result);
        System.out.println("测试12通过");
    }

    /**
     * 测试用例编号：13
     * 等价类1/8/9: 验证桥接词查找的正确性
     * 基于测试文本验证特定的桥接词关系
     */
    @Test
    void testQueryBridgeWords_SpecificBridgeWords() {
        String result = lab.queryBridgeWords("scientist", "data");
        if (result.startsWith("The bridge words from")) {
            assertTrue(result.contains("carefully") || result.contains("analyzed"));
        } else {
            assertEquals("No bridge words from scientist to data!", result);
        }
        System.out.println("测试13通过");
    }

    /**
     * 测试用例编号：14
     * 等价类1/8/9: 性能测试
     * 测试函数在正常输入下的响应时间
     */
    @Test
    void testQueryBridgeWords_Performance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            lab.queryBridgeWords("the", "data");
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertTrue(duration < 1000, "查询性能测试失败，耗时: " + duration + "ms");
        System.out.println("测试14通过");
    }
}