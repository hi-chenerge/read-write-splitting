# MySQL 读写分离之主从延迟的处理

## 

源于知乎上我的一个回答  [线上MySQL读写分离，出现写完读不到问题如何解决？](https://www.zhihu.com/question/448590078/answer/1774233156)

后续我将方案继续完善, 并在知乎上发表[MySQL读写分离，出现写完读不到问题的解决方案](https://zhuanlan.zhihu.com/p/363160460)了, 本仓库为相关源码

## 背景

应用写完主库之后, 立即读取从库, 一般是读取不到主库写入的数据的, 比如: 在一个方法中通过 2 个事务先插主库再查从库

如果是先调用插入接口插入主库, 然后调用查询接口查询从库, 可能出现一会儿能查到一会儿查不到的现象

MQ消费场景, 如果消息体不全导致消费者需要再查一次从库, 那么也会遇到查不到的问题, 这个场景本方案解决不了, 应该是让消息的属性尽量全而不要再查一次

**因为主从同步一定是有延迟的, 源于他们本来就是两个独立的数据库**

## 如何让后续的读取一定读取到主库写入的数据呢 ?

答案**一定是让后续的读取也读取主库**, 这样才能做到 100% 能读到, 但是只限于后续的若干次读取或者一段时间之内, 如果一直读主库, 那么从库不就没用了嘛

我尽量以较为优雅的方式来实现, 有问题大家可以一起交流进步~

## 环境准备

* 为了快速搭建试验环境, 我为大家准备了 MySQL 主从复制环境快速搭建脚本, 在 mysql 这个目录下, 具体步骤请参考我知乎的文章 [5分钟搭建MySQL主从复制环境](https://github.com/CyC2018/CS-Notes/blob/master/notes/计算机操作系统%20-%20目录.md), 按照我的方法, 基本上改改路径就搞定了

* 本地自行安装 Redis

## 技术栈

* SpringBoot 2.2.8.RELEASE ( 没有用最新版 )
* MyBatis ( 版本无所谓 )
* ShardingJDBC 4.x ( 因为 5.x 一直没有 GA )
* Redis ( 集中存储用户会话级别的一些数据, 用来判断某个用户是否强制走主库** )

## 一句话方案介绍

1. 某个用户新增 user 成功后, 往 Redis 存储一个标记, 代表这个用户下次查询要走主库

2. 该用户调用查询接口前先判断 Redis 中有没有强制走主库的标识, 如果有那后续执行的 SQL 强制走主库
3. 超过次数或者时间阈值后恢复读从库
4. 加一个强制走主库的全局开关, 防止主从延迟长时间过大, 需要监控系统配合来调用你的接口打开全局强制走主库

具体请看知乎的文章 [MySQL读写分离，出现写完读不到问题的解决方案](https://www.zhihu.com/question/448590078/answer/1774233156)

## 用到的知识点

**ThreadLocal**

框架级代码必备技术, 线程级别无需传递参数

**MyBatis Interceptor**

执行 SQL 之前后搞一些小动作

**Spring AOP**

用自定义注解实现无侵入性逻辑

## 项目结构说明

read-write-splitting-sample 为可运行的例子

read-write-splitting-sample/create_table.sql 是例子中的建表脚本, 请在搭建好的主库上执行

**包结构**

主要说 com.chenerge.save_after_read_consistent.city.common 包

interceptor: 包含 AOP 注解和切面以及 Mybatis Inteceptor

session: 包含当前用户 session 以及存取类以及是否走主库的存取类

web: 包含 request 监听以及简化版的认证 filter

# 测试

启动 Springboot 项目, 执行 SaveAfterReadConsistentApplication 的 main 方法

1. 用 user 1 先添加一个用户, 这里的 `token:1` 代表 user 1

```
curl -XPOST http://localhost:8080/city/save?name=chenerge1 -H 'token:1'
```

观察日志确实是插入的主库, 日志如下, 且 redis 中有了相关 key, 进入 redis-cli 执行 get read_master_cnt:1 返回 3 , 代表可以读 3 次主库

```
Actual SQL: master ::: insert into city (name) values( ? )
```

2. 用 user 1 做个查询

```
curl -XGET http://localhost:8080/city/findAll -H 'token:1'
```

发现确实走的主库, 日志如下

```
Actual SQL: master ::: select id, name from city
```

3. 用 user 2 调用有 @MasterReadIfNeeded  注解的查询接口, `token:2`代表 user 2

```
curl -XGET http://localhost:8080/city/findAll -H 'token:2'
```

发现走的从库, 日志如下, 没啥问题, 因为刚才是 user 1 做的插入, 而不是 user 2

```
Actual SQL: slave0 ::: select id, name from city
```

4. 用 user 1 调用没有 @MasterReadIfNeeded 注解的查询接口

```
curl -XGET http://localhost:8080/city/findAll0 -H 'token:1'
```

发现依然走的从库, 日志如下

```
Actual SQL: slave0 ::: select id, name from city
```

5. 最后用 user 1 连续调用 3 次有注解的查询接口, 发现最后一次变为了读取 slave, 大功告成 ~

```
curl -XGET http://localhost:8080/city/findAll -H 'token:1'
```











