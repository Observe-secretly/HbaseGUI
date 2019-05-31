更多文档请转到[Wiki](https://github.com/914245697/HbaseGUI/wiki)

# 简介
HbaseGUI可视化工具，通过Hbase-client直接操作Hbase。提供可视化查询、元数据管理和支持预分区建表三大功能

# 特点
* 响应式设计
* Hbase数据/操作可视化
* 提供包括Rowkey、版本号、Scan时间和各类Filter在内的高级查询
* 提供元数据管理
* 提供命名空间管理
* 创建表时支持通过设置起止Rowkey和分区数的方式进行预分区
* Hbase版本支持社区版（作者使用Ambari Hbase做的测试）和阿里云版
* 支持v1.*~v2.*版本Hbase（2.*版本支持将会在19年6月1日后提供，作者想过完节再来写）
* 支持版本热切换功能。即：不重启GUI程序的情况下，动态切换不同版本的Hbase数据库
* 原生支持黑暗主题（致谢Jmeter）

# 架构图
![](https://github.com/914245697/HbaseGUI/blob/master/README_IMAGE/invok-flow.png)
* 工程分为三部分组成：`HBaseGUI`Swing主程序，`Hbase-adapter-interface`适配器接口层和`Hbase-adapter`适配器
* `HBaseGUI`Swing主程序完成GUI层全部功能实现
* `Hbase-adapter-interface`适配器接口层被上下层依赖，`HBaseGUI`通过依赖它获取访问Hbase标准接口
* `HBaseGUI`通过集成[JCL](https://github.com/kamranzafar/JCL/)实现HbaseClient&适配器多版本热切换功能
* `Hbase-adapter`适配器实现了`Hbase-adapter-interface`全部接口，除公共实体外，其中包括`FilterFactoryInterface`和`HbaseAdapterInterface`关键接口

# 适配器Git库地址
* [Hbase-adapter-interface](https://github.com/914245697/HbaseGui-driver-adapter-entity)
* [Hbase-adapter](https://github.com/914245697/HbaseGui-driver-adapter)

# 目录结构
```
Hbase-GUI
  |---> bin     OSX/Linux 执行bash bin/start.sh启动。Window双击start.bat启动
  |---> conf    remote-driver.properties配置适配器地址.hbase-conf-*.conf各环境hbase配置信息
  |---> driver  存放从网络获取的适配器和Hbase-client相关jar包
  |---> img     软件内使用到的图标(16*16)
  |---> jars    主程序
```
* 首次运行软件，选择相应的hbase版本时。软件会去mvnrepository.com下载Hbase-client以及依赖。根据请保证网络畅通
* 内网环境可以选择在外网下提前下载，并拷贝到内网运行


# 下载
下载请转到[Release-history](https://github.com/914245697/HbaseGUI/wiki/Release-history)查看

# 软件图文介绍(各版本可能存在差异)

![](https://github.com/914245697/HbaseGUI/blob/master/README_IMAGE/login.png)

![](https://github.com/914245697/HbaseGUI/blob/master/README_IMAGE/cluster.png)

![](https://github.com/914245697/HbaseGUI/blob/master/README_IMAGE/queryTab.png)

![](https://github.com/914245697/HbaseGUI/blob/master/README_IMAGE/metaTab.png)

![](https://github.com/914245697/HbaseGUI/blob/master/README_IMAGE/createTab.png)