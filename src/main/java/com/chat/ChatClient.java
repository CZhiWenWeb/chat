package com.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 16:00
 * @UpdeteTime: 2020-06-08 16:00
 * @Description:
 */
public class ChatClient extends Frame {
	private TextField tfField;
	private TextArea taContent;

	private void initFrame(int x, int y, int w, int h) {
		this.tfField = new TextField();
		this.taContent = new TextArea();
		this.setBounds(x, y, w, h);
		this.setLayout(new BorderLayout());
		//添加窗口关闭监听
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				System.exit(0);
			}
		});
		this.taContent.setEditable(false);
		this.add(tfField, BorderLayout.SOUTH);
		this.add(taContent, BorderLayout.NORTH);
		//添加回车监听
		this.tfField.addActionListener(actionE -> {
			String str = tfField.getText().trim();
			taContent.append("hhh\r\n");
		});
		this.pack();
		this.setVisible(true);
		JOptionPane.showMessageDialog(this, "hhh");
		JOptionPane.showInputDialog("shuru");

	}

	public static void main(String[] args) {
		ChatClient cc = new ChatClient();
		cc.initFrame(10, 10, 10, 10);
	}

}
