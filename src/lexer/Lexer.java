package lexer;

import java.util.ArrayList;
import java.util.List;

import transition_diagram.TransitionDiagram;

/**
 * 词法分析器
 * @author qiujunfu
 *
 */
public class Lexer {
	
	private TransitionDiagram td;	// 状态转换表
	
	public Lexer() {
		// FA转换表文件路径
		String[] FA_paths = new String[] {"src/FA_transition_diagrams/identifier.txt",
				  				 "src/FA_transition_diagrams/keywords.txt",
				  				 "src/FA_transition_diagrams/number_DFA.txt",
				  				 "src/FA_transition_diagrams/octal.txt",
				  				 "src/FA_transition_diagrams/operator.txt",
				  				 "src/FA_transition_diagrams/delimiter.txt",
				  				 "src/FA_transition_diagrams/comment.txt", 
				  				 "src/FA_transition_diagrams/stringConstant.txt",
				  				 "src/FA_transition_diagrams/hex.txt",
				  				 "src/FA_transition_diagrams/characterConstant.txt"
				 				};
		td = new TransitionDiagram();
		td.createTransitionDiagram(FA_paths);
	}
	
	// 词法分析过程（模拟DFA）
	public List<List<String>> scan(String program) {
	    int state = 0;
	    int forward = 0, lexemeBegin = 0;
	    int whiteSpaceInComment = 0, whiteSpaceInQuotation = 0, lineFlag = 0;
	    int lastFinalState = 0, lastPos = -1;
	    String ch = null;
	    int row = 1, offset = 0;	// 错误定位
	    
	    List<String> tokens = new ArrayList<String>();
		List<String> errors = new ArrayList<String>();
		List<List<String>> results = new ArrayList<List<String>>();
	    
	    while (forward < program.length()) {
	    	// 跳过空白符
	    	if (Character.isWhitespace(program.charAt(forward))) {
	    		// 定位行号和偏移量
	    		if (program.charAt(forward) == '\n') {
	    			lineFlag = 0;
	    			row++;
	    			offset = 0;
	    		} else {
	    			offset++;
	    		}
	    		forward++;
	    		if (whiteSpaceInComment == 0 && whiteSpaceInQuotation == 0) {
	    			lexemeBegin = forward;
	    		}
	    		continue;
	    	}
	    	// 处理注释
	    	if (forward + 2 <= program.length() && (program.substring(forward, forward + 2).equals("/*") || 
	            program.substring(forward, forward + 2).equals("*/"))) {
	    		ch = program.substring(forward, forward + 2);
	    		// 处理注释内的空白符
	    		if (ch.equals("/*")) {
	    			whiteSpaceInComment = 1;
	    		} else {
	    			whiteSpaceInComment = 0;
	    		}
	    		forward++;
	    		offset++;
	    	} else {
	    		ch = String.valueOf(program.charAt(forward));
	    		if (ch.equals("\"")) {
	    			if (lineFlag == 0) {
	    				lineFlag = 1;
	    			} else {
	    				lineFlag = 0;
	    			}
	    			// 处理引号(字符串常量)内的空白符
	    			if (whiteSpaceInQuotation == 0) {
	    				whiteSpaceInQuotation = 1;
	    			} else {
	    				whiteSpaceInQuotation = 0;
	    			}
	    		}
	    	}
	    	state = td.move(state, ch);
	    	if (td.isFinalState(state)) {
	    		lastFinalState = state;
	    		lastPos = forward;
	    	}
	    	if (forward + 1 < program.length()) {
	    		if (td.move(state, String.valueOf(program.charAt(forward + 1))) == -1) {
	    			// 最长前缀匹配
	    			if (td.isFinalState(state)) {
	    				String str = program.substring(lexemeBegin, forward + 1);
	    				String token = td.getToken(state, str);
	    				if (lineFlag == 1) {
	    					errors.add("ERROR [row: " + row + ", " + "offset: " + (offset + 1) + "]\tstring constant格式错误！");
	    					lineFlag = 0;
	    				} else if (!token.equals("COMMENT")) {
	    					tokens.add(str + "\t" + token);
	    				}
	    				//System.out.println(str + "\t" + t.getToken(state, str));
	    				lexemeBegin = forward + 1;
	    			} else {
	    				//System.out.println("error!");
	    				if (lastFinalState != 0) {
	    					String str = program.substring(lexemeBegin, lastPos + 1);
	    					String token = td.getToken(lastFinalState, str);
	    					if (lineFlag == 1) {
	    						errors.add("ERROR [row: " + row + ", " + "offset: " + (lastPos + 1) + "]\tstring constant格式错误！");
	    						lineFlag = 0;
	    					} else if (!token.equals("COMMENT")) {
	    						tokens.add(str + "\t" + token);
	    					}
	    					lexemeBegin = lastPos + 1;
	    					forward = lastPos;
	    				} else {
	    					String errorType = td.getType(state);
	    					String errorInfo;
	    					if (errorType == null) {
	    						errorInfo = "非法字符！";
	    					} else {
	    						errorInfo = errorType + "格式错误!";
	    					}
	    					errors.add("ERROR [row: " + row + ", " + "offset: " + (offset + 1) + "]\t" +  errorInfo);
	    					lexemeBegin = forward + 1;
	    				}
	    			}
	    			state = 0;
    				lastFinalState = 0;
    				lastPos = -1;
	    		}
	    	} else if (lastFinalState != 0) {
	    		String str = program.substring(lexemeBegin, lastPos + 1);
	    		String token = td.getToken(lastFinalState, str);
	    		if (lineFlag == 1) {
	    			errors.add("ERROR [row: " + row + ", " + "offset: " + (lastPos + 1) + "]\tstring constant格式错误！");
	    			lineFlag = 0;
	    		} else if (!token.equals("COMMENT")) {
	    			tokens.add(str + "\t" + token);
	    		}
	    		state = 0;
	    		lastFinalState = 0;
	    		lexemeBegin = lastPos + 1;
	    		forward = lastPos;
	    		lastPos = -1;
	    	}
	    	forward++;
	    	offset++;
	    }
	    String str = program.substring(lexemeBegin, forward);
	    if (lexemeBegin != forward && !Character.isWhitespace(str.charAt(0))) {
	    	if (td.isFinalState(state)) {
	    		String token = td.getToken(state, str);
	    		if (lineFlag == 1) {
	    			lineFlag = 0;
	    			errors.add("ERROR [row: " + row + ", " + "offset: " + offset + "]\tstring constant格式错误！");
	    		} else if (!token.equals("COMMENT")) {
	    			tokens.add(str + "\t" + token);
	    		}
	    		//System.out.println(str + "\t" + t.getToken(state, str));
	    	} else {
	    		//System.out.println("error!");
	    		String errorType = td.getType(state);
	    		String errorInfo;
	    		if (errorType == null) {
	    			errorInfo = "非法字符！";
	    		} else {
	    			errorInfo = errorType + "格式错误!";
	    		}
	    		errors.add("ERROR [row: " + row + ", " + "offset: " + offset + "]\t" + errorInfo);
	    	}
	    }
	    
	    results.add(tokens);
	    results.add(errors);
	    return results;
	}

}
