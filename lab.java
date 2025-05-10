import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class lab {
    // 图结构定义
    private static class Graph {
        // 单词到索引的映射
        private Map<String, Integer> wordToIndex;
        // 索引到单词的映射
        private Map<Integer, String> indexToWord;
        // 使用Map存储邻接表，而不是邻接矩阵
        private Map<Integer, Map<Integer, Integer>> adjacencyList;
        // 节点数量
        private int nodeCount;

        public Graph() {
            wordToIndex = new HashMap<>();
            indexToWord = new HashMap<>();
            adjacencyList = new HashMap<>();
            nodeCount = 0;
        }

        // 添加节点
        public int addNode(String word) {
            String lowercaseWord = word.toLowerCase();
            if (!wordToIndex.containsKey(lowercaseWord)) {
                wordToIndex.put(lowercaseWord, nodeCount);
                indexToWord.put(nodeCount, lowercaseWord);
                adjacencyList.put(nodeCount, new HashMap<>());
                nodeCount++;
            }
            return wordToIndex.get(lowercaseWord);
        }

        // 添加边或增加边的权重
        public void addEdge(int from, int to) {
            Map<Integer, Integer> neighbors = adjacencyList.get(from);
            neighbors.put(to, neighbors.getOrDefault(to, 0) + 1);
        }

        // 获取节点数量
        public int getNodeCount() {
            return nodeCount;
        }

        // 获取边的权重
        public int getEdgeWeight(int from, int to) {
            Map<Integer, Integer> neighbors = adjacencyList.get(from);
            return neighbors.getOrDefault(to, 0);
        }

        // 根据单词获取索引
        public Integer getIndex(String word) {
            return wordToIndex.get(word.toLowerCase());
        }

        // 根据索引获取单词
        public String getWord(int index) {
            return indexToWord.get(index);
        }

        // 获取所有单词
        public Set<String> getAllWords() {
            return wordToIndex.keySet();
        }

        // 获取出边列表
        public java.util.List<Integer> getOutNeighbors(int nodeIndex) {
            Map<Integer, Integer> neighbors = adjacencyList.get(nodeIndex);
            return new ArrayList<>(neighbors.keySet());
        }

        // 获取入边列表
        public java.util.List<Integer> getInNeighbors(int nodeIndex) {
            java.util.List<Integer> inNeighbors = new ArrayList<>();
            for (int i = 0; i < nodeCount; i++) {
                if (adjacencyList.get(i).containsKey(nodeIndex)) {
                    inNeighbors.add(i);
                }
            }
            return inNeighbors;
        }
    }

    private static Graph graph;
    private static Random random = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath;

        // 处理命令行参数或用户输入
        if (args.length > 0) {
            filePath = args[0];
        } else {
            System.out.println("请输入文本文件路径：");
            filePath = scanner.nextLine();
        }

        // 读取文件并构建图
        try {
            String text = readFile(filePath);
            graph = buildGraph(text);
            System.out.println("图构建完成！");
            
            // 显示有向图的文本描述
            showDirectedGraph(graph);

            // 主菜单循环
            boolean quit = false;
            while (!quit) {
                System.out.println("\n请选择操作：");
                System.out.println("1. 生成有向图PNG图片");
                System.out.println("2. 查询桥接词");
                System.out.println("3. 生成新文本");
                System.out.println("4. 计算最短路径");
                System.out.println("5. 计算PageRank值");
                System.out.println("6. 随机游走");
                System.out.println("0. 退出");
                System.out.print("请输入选项：");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // 消耗换行符
                
                switch (choice) {
                    case 1:
                        // 生成并显示有向图
                        String pngFilePath = generateGraphImage(graph);
                        if (pngFilePath != null) {
                            // 显示生成的PNG图片
                            displayPngImage(pngFilePath);
                        }
                        break;
                    case 2:
                        System.out.print("请输入第一个单词：");
                        String word1 = scanner.nextLine();
                        System.out.print("请输入第二个单词：");
                        String word2 = scanner.nextLine();
                        String bridgeWords = queryBridgeWords(word1, word2);
                        System.out.println(bridgeWords);
                        break;
                    case 3:
                        System.out.print("请输入文本：");
                        String inputText = scanner.nextLine();
                        String newText = generateNewText(inputText);
                        System.out.println("生成的新文本：" + newText);
                        break;
                    case 4:
                        System.out.print("请输入起始单词：");
                        String start = scanner.nextLine();
                        System.out.print("请输入目标单词（如果为空则计算到所有单词的最短路径）：");
                        String end = scanner.nextLine();
                        if (end.trim().isEmpty()) {
                            // 计算一个单词到所有其他单词的最短路径
                            for (String targetWord : graph.getAllWords()) {
                                if (!targetWord.equals(start.toLowerCase())) {
                                    String path = calcShortestPath(start, targetWord);
                                    System.out.println("从 " + start + " 到 " + targetWord + ": " + path);
                                }
                            }
                        } else {
                            String shortestPath = calcShortestPath(start, end);
                            System.out.println(shortestPath);
                        }
                        break;
                    case 5:
                        System.out.print("请输入要计算PageRank值的单词：");
                        String word = scanner.nextLine();
                        Double prValue = calPageRank(word);
                        if (prValue != null) {
                            System.out.println(word + " 的PageRank值为: " + prValue);
                        }
                        break;
                    case 6:
                        String walk = randomWalk();
                        System.out.println("随机游走路径: " + walk);
                        // 保存到文件
                        String walkFilePath = "randomWalk.txt";
                        try (FileWriter writer = new FileWriter(walkFilePath)) {
                            writer.write(walk);
                            System.out.println("随机游走结果已保存到文件: " + walkFilePath);
                        } catch (IOException e) {
                            System.out.println("保存随机游走结果失败: " + e.getMessage());
                        }
                        break;
                    case 0:
                        quit = true;
                        break;
                    default:
                        System.out.println("无效选项，请重新选择！");
                }
            }
        } catch (IOException e) {
            System.out.println("读取文件错误: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    // 读取文件内容
    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" "); // 将换行符替换为空格
            }
        }
        return content.toString();
    }

    // 构建图
    private static Graph buildGraph(String text) {
        // 预处理文本：将标点符号替换为空格，去除非字母字符
        text = text.replaceAll("[\\p{Punct}]", " ");
        
        // 分割单词
        String[] words = text.split("\\s+");
        
        // 过滤单词，只保留字母
        java.util.List<String> filteredWords = new ArrayList<>();
        Pattern pattern = Pattern.compile("[A-Za-z]+");
        for (String word : words) {
            if (pattern.matcher(word).matches() && !word.isEmpty()) {
                filteredWords.add(word);
            }
        }
        
        // 构建图
        Graph g = new Graph();
        
        // 添加节点和边
        if (filteredWords.size() >= 2) {
            for (int i = 0; i < filteredWords.size() - 1; i++) {
                String currentWord = filteredWords.get(i);
                String nextWord = filteredWords.get(i + 1);
                
                int fromIndex = g.addNode(currentWord);
                int toIndex = g.addNode(nextWord);
                
                g.addEdge(fromIndex, toIndex);
            }
        }
        
        return g;
    }

    // 展示有向图
    public static void showDirectedGraph(Graph g) {
        if (g == null || g.getNodeCount() == 0) {
            System.out.println("图为空！");
            return;
        }
        
        System.out.println("\n有向图结构：");
        System.out.println("节点数量: " + g.getNodeCount());
        System.out.println("边列表（格式：源节点 -> 目标节点 [权重]）：");
        
        for (int i = 0; i < g.getNodeCount(); i++) {
            String sourceWord = g.getWord(i);
            boolean hasOutEdges = false;
            
            for (int j = 0; j < g.getNodeCount(); j++) {
                int weight = g.getEdgeWeight(i, j);
                if (weight > 0) {
                    String targetWord = g.getWord(j);
                    System.out.println(sourceWord + " -> " + targetWord + " [" + weight + "]");
                    hasOutEdges = true;
                }
            }
            
            if (!hasOutEdges) {
                System.out.println(sourceWord + " (无出边)");
            }
        }
    }

    // 查询桥接词
    public static String queryBridgeWords(String word1, String word2) {
        if (graph == null) {
            return "图未初始化！";
        }
        
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        
        Integer index1 = graph.getIndex(word1);
        Integer index2 = graph.getIndex(word2);
        
        if (index1 == null || index2 == null) {
            return "No " + (index1 == null ? word1 : "") + 
                   (index1 == null && index2 == null ? " or " : "") + 
                   (index2 == null ? word2 : "") + " in the graph!";
        }
        
        // 如果是同一个单词，没有桥接词
        if (index1.equals(index2)) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }
        
        // 使用DFS找到一条从word1到word2的路径
        java.util.List<Integer> path = new ArrayList<>();
        java.util.Set<Integer> visited = new HashSet<>();
        
        // 初始路径包含起始节点
        path.add(index1);
        visited.add(index1);
        
        // 执行DFS，找到一条路径即可
        boolean found = dfs(index1, index2, path, visited);
        
        if (!found || path.size() <= 2) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            // 提取路径中的桥接词（第一个和最后一个单词之间的所有节点）
            java.util.List<String> bridgeWords = new ArrayList<>();
            for (int i = 1; i < path.size() - 1; i++) {
                bridgeWords.add(graph.getWord(path.get(i)));
            }
            
            StringBuilder result = new StringBuilder("The bridge words from " + word1 + " to " + word2 + " are: ");
            
            for (int i = 0; i < bridgeWords.size(); i++) {
                if (i > 0) {
                    if (i == bridgeWords.size() - 1) {
                        result.append(" and ");
                    } else {
                        result.append(", ");
                    }
                }
                result.append(bridgeWords.get(i));
            }
            result.append(".");
            
            return result.toString();
        }
    }
    
    // DFS辅助方法：寻找从当前节点到目标节点的路径
    private static boolean dfs(int current, int target, java.util.List<Integer> path, java.util.Set<Integer> visited) {
        // 如果找到目标节点，返回true
        if (current == target) {
            return true;
        }
        
        // 获取当前节点的所有出边邻居
        java.util.List<Integer> neighbors = graph.getOutNeighbors(current);
        
        // 探索每个未访问的邻居
        for (int neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                // 添加邻居到路径并标记为已访问
                path.add(neighbor);
                visited.add(neighbor);
                
                // 从邻居继续寻找路径
                if (dfs(neighbor, target, path, visited)) {
                    return true; // 找到路径
                }
                
                // 如果没找到路径，回溯
                path.remove(path.size() - 1);
                visited.remove(neighbor);
            }
        }
        
        return false; // 所有邻居都探索完毕仍未找到路径
    }

    // 根据桥接词生成新文本
    public static String generateNewText(String inputText) {
        if (graph == null) {
            return "图未初始化！";
        }
        
        // 处理输入文本
        inputText = inputText.replaceAll("[\\p{Punct}]", " ");
        String[] words = inputText.split("\\s+");
        
        // 过滤单词
        java.util.List<String> filteredWords = new ArrayList<>();
        Pattern pattern = Pattern.compile("[A-Za-z]+");
        for (String word : words) {
            if (pattern.matcher(word).matches() && !word.isEmpty()) {
                filteredWords.add(word);
            }
        }
        
        // 如果少于两个单词，无法插入桥接词
        if (filteredWords.size() < 2) {
            return inputText;
        }
        
        StringBuilder newText = new StringBuilder(filteredWords.get(0));
        
        // 处理每对相邻单词
        for (int i = 0; i < filteredWords.size() - 1; i++) {
            String currentWord = filteredWords.get(i);
            String nextWord = filteredWords.get(i + 1);
            
            // 获取桥接词
            String bridgeWordsResult = queryBridgeWords(currentWord, nextWord);
            
            // 如果存在桥接词
            if (bridgeWordsResult.startsWith("The bridge words from")) {
                try {
                    // 提取桥接词列表
                    String bridgeWordsPart = bridgeWordsResult.substring(
                        bridgeWordsResult.indexOf("are: ") + 5, 
                        bridgeWordsResult.length() - 1  // 排除最后的句点
                    );
                    
                    // 正确处理多个桥接词
                    java.util.List<String> bridgeWordsList = new ArrayList<>();
                    
                    // 处理包含"and"的情况
                    if (bridgeWordsPart.contains(" and ")) {
                        String[] parts = bridgeWordsPart.split(" and ");
                        // 添加最后一个词
                        bridgeWordsList.add(parts[parts.length - 1].trim());
                        
                        // 处理前面的部分（可能包含逗号分隔的多个词）
                        if (parts.length > 1) {
                            String beforeAnd = parts[0];
                            if (beforeAnd.contains(", ")) {
                                String[] commaWords = beforeAnd.split(", ");
                                for (String word : commaWords) {
                                    bridgeWordsList.add(word.trim());
                                }
                            } else {
                                bridgeWordsList.add(beforeAnd.trim());
                            }
                        }
                    } else if (bridgeWordsPart.contains(", ")) {
                        // 只有逗号分隔的情况
                        String[] commaWords = bridgeWordsPart.split(", ");
                        for (String word : commaWords) {
                            bridgeWordsList.add(word.trim());
                        }
                    } else {
                        // 只有一个词的情况
                        bridgeWordsList.add(bridgeWordsPart.trim());
                    }
                    
                    // 随机选择一个桥接词
                    if (!bridgeWordsList.isEmpty()) {
                        String randomBridgeWord = bridgeWordsList.get(random.nextInt(bridgeWordsList.size()));
                        newText.append(" ").append(randomBridgeWord);
                    }
                } catch (Exception e) {
                    System.out.println("解析桥接词时出错: " + e.getMessage());
                    System.out.println("原始结果: " + bridgeWordsResult);
                }
            }
            
            // 添加下一个单词
            newText.append(" ").append(nextWord);
        }
        
        return newText.toString();
    }

    // 计算最短路径
    public static String calcShortestPath(String word1, String word2) {
        if (graph == null) {
            return "图未初始化！";
        }
        
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        
        Integer startIndex = graph.getIndex(word1);
        Integer endIndex = graph.getIndex(word2);
        
        if (startIndex == null || endIndex == null) {
            return "No " + (startIndex == null ? word1 : "") + 
                   (startIndex == null && endIndex == null ? " or " : "") + 
                   (endIndex == null ? word2 : "") + " in the graph!";
        }
        
        // 使用Dijkstra算法计算最短路径
        int n = graph.getNodeCount();
        double[] distance = new double[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];
        
        // 初始化
        Arrays.fill(distance, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        distance[startIndex] = 0;
        
        // Dijkstra算法
        for (int i = 0; i < n; i++) {
            // 找到距离最小的未访问节点
            int u = -1;
            double minDist = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && distance[j] < minDist) {
                    u = j;
                    minDist = distance[j];
                }
            }
            
            if (u == -1 || u == endIndex) break;
            
            visited[u] = true;
            
            // 更新邻居节点的距离
            for (int v = 0; v < n; v++) {
                int weight = graph.getEdgeWeight(u, v);
                if (weight > 0 && !visited[v]) {
                    double newDist = distance[u] + (1.0 / weight); // 使用边权的倒数作为距离，这样权重越大，距离越小
                    if (newDist < distance[v]) {
                        distance[v] = newDist;
                        prev[v] = u;
                    }
                }
            }
        }
        
        // 检查是否存在路径
        if (distance[endIndex] == Double.POSITIVE_INFINITY) {
            return "No path from " + word1 + " to " + word2 + "!";
        }
        
        // 重建路径
        java.util.List<Integer> path = new ArrayList<>();
        for (int at = endIndex; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        
        // 转换为单词路径
        StringBuilder pathStr = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) pathStr.append(" -> ");
            pathStr.append(graph.getWord(path.get(i)));
        }
        
        // 计算路径长度（边权值之和）
        double pathLength = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int weight = graph.getEdgeWeight(path.get(i), path.get(i + 1));
            pathLength += weight;
        }
        
        return "最短路径: " + pathStr.toString() + "\n路径长度: " + pathLength;
    }

    // 计算PageRank值
    public static Double calPageRank(String word) {
        if (graph == null) {
            System.out.println("图未初始化！");
            return null;
        }
        
        word = word.toLowerCase();
        Integer wordIndex = graph.getIndex(word);
        
        if (wordIndex == null) {
            System.out.println("单词 " + word + " 不在图中！");
            return null;
        }
        
        int n = graph.getNodeCount();
        double d = 0.85; // 阻尼系数
        double[] pr = new double[n];
        double[] newPr = new double[n];
        
        // 初始化PageRank值
        Arrays.fill(pr, 1.0 / n);
        
        // 迭代计算PageRank（使用100次迭代近似）
        for (int iter = 0; iter < 100; iter++) {
            // 计算新的PageRank值
            for (int i = 0; i < n; i++) {
                newPr[i] = (1 - d) / n;
                
                // 获取所有指向i的节点
                java.util.List<Integer> inNeighbors = graph.getInNeighbors(i);
                
                for (int j : inNeighbors) {
                    // 获取j的出度
                    java.util.List<Integer> outNeighbors = graph.getOutNeighbors(j);
                    int outDegree = outNeighbors.size();
                    
                    if (outDegree > 0) {
                        // 计算j对i的贡献
                        newPr[i] += d * pr[j] / outDegree;
                    }
                }
            }
            
            // 每次迭代后归一化PageRank值，确保总和为1
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += newPr[i];
            }
            
            // 如果总和不为0，则进行归一化
            if (sum > 0) {
                for (int i = 0; i < n; i++) {
                    newPr[i] = newPr[i] / sum;
                }
            }
            
            // 每10次迭代输出一次结果
            if (iter % 10 == 0 || iter == 99) {
                System.out.println("迭代 " + (iter+1) + ": " + word + " 的PR值 = " + newPr[wordIndex]);
            }
            
            // 更新PageRank值
            System.arraycopy(newPr, 0, pr, 0, n);
        }
        
        // 输出最终计算后的PR总和（验证是否接近1）
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += pr[i];
        }
        System.out.println("所有节点PR值总和: " + sum + " (应接近1)");
        
        return pr[wordIndex];
    }

    // 随机游走
    public static String randomWalk() {
        if (graph == null || graph.getNodeCount() == 0) {
            return "图为空！";
        }
        
        // 随机选择起始节点
        int startNodeIndex = random.nextInt(graph.getNodeCount());
        String startWord = graph.getWord(startNodeIndex);
        
        StringBuilder walkPath = new StringBuilder(startWord);
        int currentNodeIndex = startNodeIndex;
        
        // 用于记录已经走过的边
        Set<String> visitedEdges = new HashSet<>();
        Scanner scanner = new Scanner(System.in);
        
        boolean continueWalk = true;
        try {
            while (continueWalk) {
                // 获取当前节点的所有出边
                java.util.List<Integer> neighbors = graph.getOutNeighbors(currentNodeIndex);
                
                // 如果没有出边，停止游走
                if (neighbors.isEmpty()) {
                    walkPath.append("\n[随机游走结束：当前节点没有出边]");
                    break;
                }
                
                // 随机选择一个邻居
                int nextNodeIndex = neighbors.get(random.nextInt(neighbors.size()));
                String nextWord = graph.getWord(nextNodeIndex);
                
                // 构建边标识
                String edgeId = currentNodeIndex + "->" + nextNodeIndex;
                
                // 检查边是否已经走过
                if (visitedEdges.contains(edgeId)) {
                    walkPath.append("\n[随机游走结束：出现重复边 ").append(graph.getWord(currentNodeIndex))
                           .append("->").append(nextWord).append("]");
                    break;
                }
                
                // 记录边并更新路径
                visitedEdges.add(edgeId);
                walkPath.append(" ").append(nextWord);
                currentNodeIndex = nextNodeIndex;
                
                // 每走一步，打印当前路径并询问是否继续
                System.out.println("当前路径: " + walkPath.toString());
                System.out.print("按Enter继续，输入'q'停止: ");
                String input = scanner.nextLine();
                if ("q".equalsIgnoreCase(input)) {
                    walkPath.append("\n[随机游走被用户手动停止]");
                    continueWalk = false;
                }
            }
        } finally {
            scanner.close();
        }
        
        return walkPath.toString();
    }

    // 生成DOT文件并转换为PNG图片
    public static String generateGraphImage(Graph g) {
        if (g == null || g.getNodeCount() == 0) {
            System.out.println("图为空，无法生成图像！");
            return null;
        }
        
        try {
            // 创建DOT文件
            String dotFilePath = "graph.dot";
            try (FileWriter writer = new FileWriter(dotFilePath)) {
                writer.write("digraph G {\n");
                writer.write("  rankdir=TB;\n");  // 从上到下的布局
                writer.write("  node [shape=ellipse, style=filled, fillcolor=white, color=black];\n");
                writer.write("  edge [fontcolor=black];\n");
                
                // 添加边
                for (int i = 0; i < g.getNodeCount(); i++) {
                    String sourceWord = g.getWord(i);
                    for (int j = 0; j < g.getNodeCount(); j++) {
                        int weight = g.getEdgeWeight(i, j);
                        if (weight > 0) {
                            String targetWord = g.getWord(j);
                            writer.write("  \"" + sourceWord + "\" -> \"" + targetWord + 
                                         "\" [label=\"" + weight + "\", weight=" + weight + "];\n");
                        }
                    }
                }
                
                writer.write("}\n");
            }
            
            System.out.println("DOT文件已生成: " + dotFilePath);
            
            // 使用Graphviz的绝对路径
            String dotExePath = "D:\\Graphviz\\bin\\dot.exe";
            String pngFilePath = "graph.png";
            
            try {
                ProcessBuilder pb = new ProcessBuilder(dotExePath, "-Tpng", "-o", pngFilePath, dotFilePath);
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    System.out.println("PNG图片已生成: " + pngFilePath);
                    return pngFilePath;
                } else {
                    System.out.println("转换PNG失败，exit code: " + exitCode);
                    System.out.println("请确认D:\\Graphviz\\bin\\dot.exe是否存在");
                    return null;
                }
            } catch (Exception e) {
                System.out.println("转换PNG失败: " + e.getMessage());
                System.out.println("请检查Graphviz路径是否正确: " + dotExePath);
                System.out.println("您可以手动运行命令: " + dotExePath + " -Tpng -o " + pngFilePath + " " + dotFilePath);
                return null;
            }
        } catch (IOException e) {
            System.out.println("生成图像文件失败: " + e.getMessage());
            return null;
        }
    }
    
    // 使用Swing显示PNG图片
    private static void displayPngImage(String imagePath) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("有向图可视化");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                
                // 创建图片面板
                JLabel imageLabel = new JLabel();
                ImageIcon imageIcon = new ImageIcon(imagePath);
                imageLabel.setIcon(imageIcon);
                
                // 创建滚动面板
                JScrollPane scrollPane = new JScrollPane(imageLabel);
                scrollPane.setPreferredSize(new Dimension(800, 600));
                
                frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                
                System.out.println("图像已在新窗口中显示");
            } catch (Exception e) {
                System.out.println("显示图像失败: " + e.getMessage());
                System.out.println("请手动打开图像文件: " + imagePath);
            }
        });
    }
}