# 参考:
#https://github.com/songxinjianqwe/Chat
#http://tutorials.jenkov.com/java-nio/non-blocking-server.html
#http://www.mamicode.com/info-detail-2736833.html

问题1：redis处理集中写入请求大量失败
    redis通过多个socket进行连接，使用selector进行就绪监听。重复创建和销毁socket成本高;
    使用连接池代替单个连接，并初始化最小就绪连接

问题2：多线程下server向client发送多次相同的消息
    多个线程操作同一个ByteBuffer导致读取错误的信息；
    为每个IO线程提供独立的读写ByteBuffer

实现可调整大小的缓冲区的几种方式:

1.按副本大小调整大小，如果无发放入小的缓冲区，则分配一个较大的缓冲区，优点是一条
消息都保存在一个连续的字节数组中，解析消息更加容易；缺点是对于较大的消息需要复制
大量数据，为了减少复制，可以根据流经系统的消息大小找到减少复制数量的缓冲区大小。

2.Type Length Value编码消息
TLV编码是内存管理更加容易。立即就知道要为该消息分配多少内存。缺点是在消息的所有
数据到达之前需要为其分配内存，因此，一些发送大消息的速度较慢的连接会占用大量内存。
解决此问题的方法是使用一种消息格式，其中包含多个TLV字段，为每个字段分配内存，而不
是为整个消息。如果一段时间未收到消息进行超时处理，防止服务器内存耗尽。
