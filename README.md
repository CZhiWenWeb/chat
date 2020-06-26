# 参考:
#https://github.com/songxinjianqwe/Chat
#http://tutorials.jenkov.com/java-nio/non-blocking-server.html
#http://www.mamicode.com/info-detail-2736833.html

使用java.nio实现的即时通讯服务器,在并发条件下能正确的收发消息.

已实现的功能：1.消息单发与群发

测试方法：
运行Server与CountClient的main方法进行群发测试，CountClient.Num设置client数量，ClientSend.maxNum设置每个client
发送的消息数量。

采用Type Length Value编码消息
TLV编码是内存管理更加容易。立即就知道要为该消息分配多少内存。缺点是在消息的所有
数据到达之前需要为其分配内存，因此，一些发送大消息的速度较慢的连接会占用大量内存。
解决此问题的方法是使用一种消息格式，其中包含多个TLV字段，为每个字段分配内存，而不
是为整个消息。如果一段时间未收到消息进行超时处理，防止服务器内存耗尽。(超时处理未实现)

