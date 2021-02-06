package gui;

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import lexer.Lexer;

public class GUI {

	public static void main(String[] args) {
		JFrame window = new JFrame("编译器");
		window.setSize(800, 450);
		Container container = window.getContentPane();
		container.setLayout(null);
		
		Lexer lexer = new Lexer();
		
		// 程序文本框 + 滚动条
		JTextArea textOfProgram = new JTextArea();
		JScrollPane textOfProgramScroll = new JScrollPane(textOfProgram);
		
		// 错误信息表格 + 滚动条
		JTable errorInfoTable = new JTable();
		errorInfoTable.setModel(
			new DefaultTableModel(new Object[][] {}, new String[] {"错误位置", "错误信息"}) {
				private static final long serialVersionUID = 1L;
				boolean[] canEdit = new boolean[] {false, false};
				@ Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
                	return canEdit[columnIndex];
                }
			}
		);
		JScrollPane errorInfoTableScroll = new JScrollPane(errorInfoTable);
		// errorInfoTableScroll.setViewportView(errorInfoTable);
		
		
		// 分析结果 + 滚动条
		JScrollPane resultsScroll = new JScrollPane();
		JTextArea textOfResults = new JTextArea();
		textOfResults.setEditable(false);
		resultsScroll.setViewportView(textOfResults);
		
		container.add(textOfProgramScroll);
		container.add(errorInfoTableScroll);
		container.add(resultsScroll);
		
		// 设置程序文本框大小、错误信息表格大小跟随窗口大小的变动而变动
		window.addComponentListener(
			new ComponentAdapter() {
				@ Override
				public void componentResized(ComponentEvent e) {
					textOfProgramScroll.setBounds(5, 5, container.getWidth() / 2 - 10, container.getHeight() / 3 * 2 - 10);
					errorInfoTableScroll.setBounds(5, container.getHeight() / 3 * 2, container.getWidth() / 2 - 10, container.getHeight() / 3 - 5);
					resultsScroll.setBounds(container.getWidth() / 2, 5, container.getWidth() / 2 - 5, container.getHeight() - 10);
				}
			}
		);
		
		// 菜单设置
		JMenuBar menuBar = new JMenuBar();
		
		// 打开程序文件
		JMenu openMenu = new JMenu();
		JMenuItem openProgramFile = new JMenuItem();
		openProgramFile.addActionListener(
			new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					FileDialog fileDialog;
					java.awt.Frame frame = null;
					fileDialog = new FileDialog(frame, "Open", FileDialog.LOAD);
					fileDialog.setVisible(true);
					try {
						textOfProgram.setText("");		// 清空
						BufferedReader file = new BufferedReader(new FileReader(new File(fileDialog.getDirectory(), fileDialog.getFile())));
						String line;
						while ((line = file.readLine()) != null) {
							textOfProgram.append(line + "\r\n");
						}
						file.close();
					} catch (IOException e) {
						System.out.println(e);
					}
				}
			}
		);
		openProgramFile.setText("Program File");
		openMenu.setText("Open");
		openMenu.add(openProgramFile);
		menuBar.add(openMenu);
		
		// 清空内容
		JMenu editMenu = new JMenu();
		JMenuItem clearContent = new JMenuItem();
		clearContent.addActionListener(
			new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					textOfProgram.setText("");
					DefaultTableModel errorInfoTableModel = (DefaultTableModel) errorInfoTable.getModel();
					errorInfoTableModel.setRowCount(0);
					errorInfoTable.invalidate();
					textOfResults.setText("");
					resultsScroll.setViewportView(textOfResults);
				}
			}
		);
		clearContent.setText("Clear");
		editMenu.add(clearContent);
		editMenu.setText("Edit");
		menuBar.add(editMenu);
		
		// 分析
		JMenu analysisMenu = new JMenu();
		JMenuItem lexicalAnalysis = new JMenuItem();
		lexicalAnalysis.addActionListener(
			new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					String program = textOfProgram.getText();
					JTable tokensTable = new JTable();
					tokensTable.setModel(
						new DefaultTableModel(new Object[][]{}, new String[]{"词素","<种别码, 属性值>"}) {
							private static final long serialVersionUID = 1L;
							boolean[] canEdit = new boolean[] {false, false};
							@ Override
							public boolean isCellEditable(int rowIndex, int columnIndex) {
								return canEdit[columnIndex];
							}
						}
					);
					DefaultTableModel tokensTableModel = (DefaultTableModel) tokensTable.getModel();
					tokensTableModel.setRowCount(0);
					tokensTable.invalidate();
					DefaultTableModel errorInfoTableModel = (DefaultTableModel) errorInfoTable.getModel();
					errorInfoTableModel.setRowCount(0);
					errorInfoTable.invalidate();
					
					if (program.length() > 0) {
						List<List<String>> results = lexer.scan(program);
						List<String> tokens = results.get(0);
						List<String> errors = results.get(1);
						// 打印词法分析结果
						if (tokens.size() != 0) {
							for (String token : tokens) {
								String[] pair = token.split("\t");
								tokensTableModel.addRow(new Object[] {pair[0], pair[1]});
							}
						}
						// 打印错误信息
						if (errors.size() != 0) {
							for (String error : errors) {
								String[] pair = error.split("\t");
								errorInfoTableModel.addRow(new Object[] {pair[0], pair[1]});
							}
						}
					}
					resultsScroll.setViewportView(tokensTable);
				}
			}
		);
		lexicalAnalysis.setText("Lexical Analysis");
		analysisMenu.add(lexicalAnalysis);
		analysisMenu.setText("Analysis");
		menuBar.add(analysisMenu);
		
		window.setJMenuBar(menuBar);
		
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setVisible(true);
	}

}
