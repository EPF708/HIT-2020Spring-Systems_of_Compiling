package transition_diagram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 有穷自动机的状态转换表
 * @author qiujunfu
 *
 */
public class TransitionDiagram {
	
	  private Map<Integer, Map<String, Integer>> transitionDiagram;	// 状态转换表
	  private Map<Integer, String> finalStates;		// 终止状态
	  private Map<Integer, String> generalStates;	// 一般状态
	  private Set<String> keywords;			// 关键字
	  private int stateNum;		// 状态数量
	  
	  public TransitionDiagram() {
		  transitionDiagram = new HashMap<Integer, Map<String, Integer>>();
		  finalStates = new HashMap<Integer, String>();
		  generalStates = new HashMap<Integer, String>();
		  keywords = new HashSet<String>();
		  stateNum = 0;
	  }	  
	  /**
	   * 构建状态转换表.
	   * @param FApaths 转换表文件路径
	   */
	  public void createTransitionDiagram(String[] FApaths) {
		  // 统计状态数目以合并转换表
		  int skip = 0;
		  Map<Integer, Integer> alreadyExistStates = new HashMap<Integer, Integer>();    // 重复状态
		  for (int fileIndex = 0;fileIndex < FApaths.length;fileIndex++) {
			  String path = FApaths[fileIndex];
			  try {
				  // 读取文件中的FA转换表
				  BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
				  String firstLine = file.readLine();	// 每个文件开头第一行指示了单词类型
				  // 保存关键字
				  if (firstLine.equals("keywords")) {
					  String line = file.readLine();
					  while (line != null) {
						  keywords.add(line);
						  line = file.readLine();
					  }
				  } else {
					  skip = 0;
					  // 读取第一行获得字母表（删除第一个空白元素）
					  String[] words = file.readLine().split("\t");
					  String[] alphabet = new String[words.length - 1];
					  for (int i = 1;i < words.length;i++) {
						  alphabet[i - 1] = words[i];
					  }
					  String line = file.readLine();
					  int state = 0;
					  // 构建转换表
					  while (line != null) {
						  words = line.split("\t");
						  // 后面带*的状态为终止状态
						  // if (words[0].substring(words[0].length() - 1, words[0].length()).equals("*")) {
						  if (words[0].charAt(words[0].length() - 1) == '*') {
							  state = Integer.valueOf(words[0].substring(0, words[0].length() - 1));
							  if (state != 0) {
								  state += stateNum;
							  }
							  finalStates.put(state, firstLine);
						  // 普通状态
						  } else {
							  state = Integer.valueOf(words[0]);
							  if (state != 0) {
								  state += stateNum;
								  generalStates.put(state, firstLine);
							  }
						  }
						  // 合并重复状态
						  if (alreadyExistStates.containsKey(state)) {
							  int tmp = state;
							  state = alreadyExistStates.get(state);
							  if (finalStates.containsKey(tmp)) {
								  finalStates.put(state, finalStates.get(tmp));
							  } else {
								  generalStates.put(state, generalStates.get(tmp));
							  }
						  }
						  Map<String, Integer> cor = new HashMap<String, Integer>();
						  for (int i = 1;i < words.length;i++) {
							  if (words[i].equals("$")) {
								  continue;
							  } else {
								  int successor = Integer.valueOf(words[i]);
								  if (successor != 0) {
									  successor += stateNum;
								  }
								  cor.put(alphabet[i - 1], successor);
							  }
						  }
						  if (!cor.isEmpty()) {
							  if (transitionDiagram.containsKey(state)) {
								  // 合并Map
								  for (String key : cor.keySet()) {
									  if (!transitionDiagram.get(state).containsKey(key)) {
										  transitionDiagram.get(state).put(key, cor.get(key));
									  } else {
										  alreadyExistStates.put(cor.get(key), transitionDiagram.get(state).get(key));
									  }
								  }
							  } else {
								  transitionDiagram.put(state, cor);
							  }
						  }
						  line = file.readLine();
						  skip++;
					  }
					  stateNum += skip - 1;
				  }
				  file.close();
	      	  } catch(FileNotFoundException e) {
	      		  e.printStackTrace();
	      	  } catch(IOException e) {
	      		  e.printStackTrace();
	          }
		  }
	  }
	  
	  /**
	   * 状态转换.
	   * @param state 当前状态
	   * @param ch 当前字符
	   * @return 返回当前状态遇到当前字符的后继状态，如果不存在后继状态则返回-1
	   */
	  public int move(int state, String ch) {
		  //处理注释
		  if (transitionDiagram.containsKey(state) && transitionDiagram.get(state).containsKey("other1") && !ch.equals("*/")) {
			  return transitionDiagram.get(state).get("other1");
			  //处理字符串常量
		  } else if (transitionDiagram.containsKey(state) && transitionDiagram.get(state).containsKey("other2") && !ch.equals("\"")) {
			  return transitionDiagram.get(state).get("other2");
			  //处理字符常量
		  } else if (transitionDiagram.containsKey(state) && transitionDiagram.get(state).containsKey("char") && !ch.equals("\'")) {
			  return transitionDiagram.get(state).get("char");
		  } else if (transitionDiagram.containsKey(state) && transitionDiagram.get(state).containsKey(ch)) {
			  return transitionDiagram.get(state).get(ch);
		  } else {
			  //当前状态下遇到当前字符无后继状态
			  return -1;
		  }
	  }
	  
	  /**
	   * 判断终止状态.
	   */
	  public boolean isFinalState(int state) {
		  return finalStates.containsKey(state);
	  }
	  
	  public boolean isKeyWord(String word) {
		  return keywords.contains(word);
	  }
	  
	  /**
	   * 获得Token
	   * @param state 当前状态
	   * @param str 当前字符串
	   * @return 该字符串的token
	   */
	  public String getToken(int state, String str) {
		  String type = null, property = null;
		  switch (finalStates.get(state)) {
		  	  // 标识符
	          case "identifier":
	        	  if (isKeyWord(str)) {
	        		  type = str.toLowerCase();
	        		  property = "_";
	        	  } else {
	        		  type = "id";
	        		  property = str;
	        	  }
	        	  break;
	          // 数字
	          case "number":
	        	  type = "number";
	        	  // 实数
	        	  if (str.contains(".")) {
	        		  type = "real";
	        	  }
	        	  // 科学记数法
	        	  if (str.contains("E") || str.contains("e")) {
	        		  type = "scientific";
	        	  }
	        	  property = str;
	        	  break;
	          // 界符和操作符
	          case "delimiter":
	          case "operator":
	        	  type = str;
	        	  property = "_";
	        	  break;
	          // 字符串常量
	          case "string constant":
	        	  type = "string constant";
	        	  property = str;
	        	  break;
	          // 字符常量
	          case "character constant":
	        	  type = "character constant";
	        	  property = str;
	        	  break;
	          // 十六进制
	          case "hex":
	        	  type = "hex";
	        	  property = str;
	        	  break;
	          // 注释
	          case "comment":
	        	  return "comment";
	          // 八进制
	          case "octal":
	        	  type = "oct";
	        	  property = str;
	        	  break;
		  }
		  return "<" + type + ", " + property + ">";
	  }
	  public String getType(int state) {
		  return generalStates.get(state);
	  }
}