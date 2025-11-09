package com.ksptool.mycraft;

import com.ksptool.mycraft.core.Game;

/**
 * 程序入口类，负责启动游戏
 */
public class Launcher {
    public static void main(String[] args) {
        Game game = new Game();
        game.run();
    }
}
