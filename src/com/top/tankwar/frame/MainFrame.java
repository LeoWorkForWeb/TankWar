package com.top.tankwar.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * 主窗体
 *
 * @author 子墨
 *
 */
public class MainFrame extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 构造方法
	 */
	public MainFrame() {
		// 设置标题
		setTitle("坦克大战");
		// 设置宽高
		setSize(800, 600);
		// 不可调整大小
		setResizable(false);
		// 创建系统该默认组件工具包
		Toolkit tool = Toolkit.getDefaultToolkit();
		// 获取屏幕尺寸，赋给一个二维坐标对象
		Dimension d = tool.getScreenSize();
		// 让主窗体在屏幕中间显示
		setLocation((d.width - getWidth()) / 2, (d.height - getHeight()) / 2);
		// 关闭窗体时无操作
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addListener();// 添加事件监听
		setPanel(new LoginPanel(this));// 添加登陆面板
		//this.add(new LoginPanel(this));
		setVisible(true);
		//System.gc();
	}

	/**
	 * 添加组件监听
	 */
	private void addListener() {
		addWindowListener(new WindowAdapter() {// 添加窗体事件监听
			@Override
			public void windowClosing(WindowEvent e) {// 窗体关闭时
				int closeCode = JOptionPane.showConfirmDialog(MainFrame.this, "确定退出游戏？", "提示！",
						JOptionPane.YES_NO_OPTION);// 弹出选择对话框，并记录用户选择
				if (closeCode == JOptionPane.YES_OPTION) {// 如果用户选择确定
					System.exit(0);// 关闭程序
				}
			}
		});
	}

	/**
	 * 更换主容器中的面板
	 *
	 * @param panel
	 *              更换的面板
	 */
	public void setPanel(JPanel panel) {
		Container c = getContentPane();// 获取主容器对象
		if(panel instanceof LoginPanel) {
			panel.addKeyListener((KeyListener) panel);
		}
		c.removeAll();// 删除容器中所有组件
		c.add(panel);// 容器添加面板
		c.validate();// 容器重新验证所有组件
	}
}
