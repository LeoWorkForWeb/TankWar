package com.top.tankwar.model.wall;


import com.top.tankwar.util.ImageUtil;

/**
 * 基地
 *
 * @author 子墨
 *
 */
public class BaseWall extends Wall {
	/**
	 * 基地构造方法
	 *
	 * @param x
	 *              基地横坐标
	 * @param y
	 *              基地纵坐标
	 */
	public BaseWall(int x, int y) {
		super(x, y, ImageUtil.BASE_IMAGE_URL);// 调用父类构造方法，使用默认基地图片
	}

}
