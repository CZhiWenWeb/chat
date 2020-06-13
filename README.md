# 参考:
#https://github.com/songxinjianqwe/Chat
#http://tutorials.jenkov.com
#http://www.mamicode.com/info-detail-2736833.html

问题1：redis处理集中写入请求大量失败
    redis通过多个socket进行连接，使用selector进行就绪监听。重复创建和销毁socket成本高;
    使用连接池代替单个连接，并初始化最小就绪连接

问题2：多线程下server向client发送多次相同的消息
    多个线程操作同一个ByteBuffer导致读取错误的信息；
    为每个IO线程提供独立的读写ByteBuffer


    

