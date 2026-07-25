package com.endofdays_re.utils.tools;

import net.minecraft.network.chat.MutableComponent;


public interface Component {
    // 获取最终的组件，支持传入变量
    static MutableComponent getComponent(String msg, MessageTool.Variable<?>... variables) {
        MessageTool map = getParser();  // 获取解析器
        for (MessageTool.Variable<?> variable : variables) {
            map.setVariable(variable.key, variable.value);  // 设置变量
        }
        return map.Component(map.parse(msg));  // 生成并返回组件
    }

    static MutableComponent translatable(String msg, MessageTool.Variable<?>... variables) {
        MessageTool map = getParser();  // 获取解析器
        for (MessageTool.Variable<?> variable : variables) {
            map.setVariable(variable.key, variable.value);  // 设置变量
        }
        return map.Component(map.parse(net.minecraft.network.chat.Component.translatable(msg).getString()));  // 生成并返回组件
    }

    // 获取一个 ChatUails 解析器实例，这里每次都创建新的实例
    private static MessageTool getParser() {
        return new MessageTool();  // 这里可以改成返回一个共享的实例，或者缓存的单例
    }

    // 增加新的变量，并返回新的实例
    static <T> MessageTool addVariables(String key, T value) {
        return getParser().setVariable(key, value);  // 设置变量
    }
}
