// 定义会话存储的键名常量
SESSION_ORDER = "SESSION_ORDER";
SESSION_TICKET_PARAMS = "SESSION_TICKET_PARAMS";
SESSION_ALL_TRAIN = "SESSION_ALL_TRAIN";

/**
 * 提供对会话存储（sessionStorage）的封装，简化存储操作并增强代码可维护性
 */
SessionStorage = {
    /**
     * 根据键名获取存储的值
     * @param {string} key - 要获取值的键名
     * @returns {any} - 返回解析后的值，如果键不存在或值为undefined，则返回null
     */
    get: function (key) {
        var v = sessionStorage.getItem(key);
        if (v && typeof(v) !== "undefined" && v !== "undefined") {
            return JSON.parse(v);
        }
    },
    /**
     * 将数据存储到会话存储中
     * @param {string} key - 要存储数据的键名
     * @param {any} data - 要存储的数据，可以是任何可以被JSON序列化的值
     */
    set: function (key, data) {
        sessionStorage.setItem(key, JSON.stringify(data));
    },
    /**
     * 从会话存储中移除指定键名的数据
     * @param {string} key - 要移除数据的键名
     */
    remove: function (key) {
        sessionStorage.removeItem(key);
    },
    /**
     * 清空会话存储中的所有数据
     */
    clearAll: function () {
        sessionStorage.clear();
    }
};
