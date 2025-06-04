import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class labTestw {
    
    private InputStream originalIn;
    
    @BeforeEach
    void setUp() {
        // 保存原始输入流
        originalIn = System.in;
    }
    
    @AfterEach
    void tearDown() {
        // 恢复原始输入流
        System.setIn(originalIn);
    }
    
    // 辅助方法：通过反射设置静态graph变量
    private void setGraph(Object graph) throws Exception {
        Field graphField = lab.class.getDeclaredField("graph");
        graphField.setAccessible(true);
        graphField.set(null, graph);
    }
    
    // 辅助方法：构建测试图
    private Object buildTestGraph() throws Exception {
        // 使用现有的buildGraph方法构建图
        String testText = "the scientist carefully analyzed the data wrote a detailed report and shared the team but the team requested more data so the it again";
        Method buildGraphMethod = lab.class.getDeclaredMethod("buildGraph", String.class);
        buildGraphMethod.setAccessible(true);
        return buildGraphMethod.invoke(null, testText);
    }
    
    @Test
    void testPath1_GraphNull() throws Exception {
        // 测试用例1：覆盖路径1 - 图为空
        setGraph(null);
        
        String result = lab.randomWalk();
        assertEquals("图为空！", result);
        System.out.println("测试1通过");
    }
    
    @Test
    void testPath2_GraphEmpty() throws Exception {
        // 测试用例2：覆盖路径1 - 图节点数为0
        // 创建空图
        Class<?> graphClass = Class.forName("lab$Graph");
        Object emptyGraph = graphClass.getDeclaredConstructor().newInstance();
        setGraph(emptyGraph);
        
        String result = lab.randomWalk();
        assertEquals("图为空！", result);
        System.out.println("测试2通过");
    }
    
    @Test
    void testPath3_NoOutEdges() throws Exception {
        // 测试用例3：覆盖路径3 - 遇到无出边节点
        // 构建只有一个节点的图
        Class<?> graphClass = Class.forName("lab$Graph");
        Object singleNodeGraph = graphClass.getDeclaredConstructor().newInstance();
        Method addNodeMethod = graphClass.getDeclaredMethod("addNode", String.class);
        addNodeMethod.setAccessible(true);
        addNodeMethod.invoke(singleNodeGraph, "alone");
        
        setGraph(singleNodeGraph);
        
        // 模拟用户输入（虽然在无出边情况下不会用到）
        System.setIn(new ByteArrayInputStream("\n".getBytes()));
        
        String result = lab.randomWalk();
        assertTrue(result.contains("alone"));
        assertTrue(result.contains("随机游走结束：当前节点没有出边"));
        System.out.println("测试3通过");
    }
    
    @Test
    void testPath4_RepeatEdge() throws Exception {
        // 保存原始输出流
        PrintStream originalOut = System.out;
        // 创建新的输出流
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        // 测试用例4：覆盖路径4 - 遇到重复边
        Class<?> graphClass = Class.forName("lab$Graph");
        Object cyclicGraph = graphClass.getDeclaredConstructor().newInstance();
        Method addNodeMethod = graphClass.getDeclaredMethod("addNode", String.class);
        Method addEdgeMethod = graphClass.getDeclaredMethod("addEdge", int.class, int.class);
        addNodeMethod.setAccessible(true);
        addEdgeMethod.setAccessible(true);
        
        int nodeA = (Integer) addNodeMethod.invoke(cyclicGraph, "a");
        int nodeB = (Integer) addNodeMethod.invoke(cyclicGraph, "b");
        addEdgeMethod.invoke(cyclicGraph, nodeA, nodeB);
        addEdgeMethod.invoke(cyclicGraph, nodeB, nodeA);
        
        setGraph(cyclicGraph);
        
        // 模拟用户输入继续游走
        System.setIn(new ByteArrayInputStream("\n\n\n".getBytes()));
        
        String result = lab.randomWalk();
        assertTrue(result.contains("随机游走结束：出现重复边"));
        
        // 恢复原始输出流并打印测试通过信息
        System.setOut(originalOut);
        System.out.println("测试4通过");
    }
    
    @Test
    void testPath5_UserQuit() throws Exception {
        // 保存原始输出流
        PrintStream originalOut = System.out;
        // 创建新的输出流
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        // 测试用例5：覆盖路径5 - 用户输入q退出
        Object testGraph = buildTestGraph();
        setGraph(testGraph);
        
        // 模拟用户输入q
        System.setIn(new ByteArrayInputStream("q\n".getBytes()));
        
        String result = lab.randomWalk();
        assertTrue(result.contains("随机游走被用户手动停止"));
        
        // 恢复原始输出流并打印测试通过信息
        System.setOut(originalOut);
        System.out.println("测试5通过");
    }
}