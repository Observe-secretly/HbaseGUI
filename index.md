更多文档请转到[Wiki](https://github.com/Observe-secretly/HbaseGUI/wiki)

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
* 支持v1.*~v2.*版本Hbase（2.*版本支持将会在第一个Release发布时提供）
* 支持版本热切换功能。即：不重启GUI程序的情况下，动态切换不同版本的Hbase数据库
* 原生支持黑暗主题（致谢Jmeter）

# 架构图
![](https://raw.githubusercontent.com/Observe-secretly/HbaseGUI/gh-pages/invok-flow.png)
* 工程分为三部分组成：`HBaseGUI`Swing主程序，`Hbase-adapter-interface`适配器接口层和`Hbase-adapter`适配器
* `HBaseGUI`Swing主程序完成GUI层全部功能实现
* `Hbase-adapter-interface`适配器接口层被上下层依赖，`HBaseGUI`通过依赖它获取访问Hbase标准接口
* `HBaseGUI`通过集成[JCL](https://github.com/kamranzafar/JCL/)实现HbaseClient&适配器多版本热切换功能
* `Hbase-adapter`适配器实现了`Hbase-adapter-interface`全部接口，除公共实体外，其中包括`FilterFactoryInterface`和`HbaseAdapterInterface`关键接口

# 适配器Git库地址
* [Hbase-adapter-interface](https://github.com/Observe-secretly/HbaseGui-driver-adapter-entity)
* [Hbase-adapter](https://github.com/Observe-secretly/HbaseGui-driver-adapter)

关于二次开发部分，请[戳我](https://github.com/Observe-secretly/HbaseGUI/wiki/Continued-development)

# 目录结构
```
Hbase-GUI
  |---> bin     OSX/Linux 执行bash bin/start.sh启动。Window双击start.bat启动
  |---> conf    remote-driver.properties配置适配器地址.hbase-conf-*.conf各环境hbase配置信息
  |---> img     软件内使用到的图标(16*16)
  |---> jars    主程序
  
  Hbaes相关的驱动、适配器等jar包以及元数据配置保存在System.getProperty("user.home")/.hbase-gui-conf文件夹下
```
* 首次运行软件，选择相应的hbase版本时。软件会去mvnrepository.com下载Hbase-client以及依赖。根据请保证网络畅通
* 内网环境可以选择在外网下提前下载，并拷贝到内网运行


# 下载
下载请转到[Release-history](https://github.com/Observe-secretly/HbaseGUI/wiki/Release-history)查看

# 软件图文介绍(各版本可能存在差异)

![](https://raw.githubusercontent.com/Observe-secretly/HbaseGUI/gh-pages/login.png)

`ZNODE.PARENT参数请登录Hbase Masteer UI首页查看Zookeeper Base Path配置.默认Ambari Hbase是:/hbase-unsecure,阿里云是:/hbase；ZK.QUORUM对应Zookeeper Quorum配置；HBASE.MASTER可以不填写`

![](https://raw.githubusercontent.com/Observe-secretly/HbaseGUI/gh-pages/cluster.png)

![](https://raw.githubusercontent.com/Observe-secretly/HbaseGUI/gh-pages/desc.png)

![](https://raw.githubusercontent.com/Observe-secretly/HbaseGUI/gh-pages/queryTab.png)

![](https://raw.githubusercontent.com/Observe-secretly/HbaseGUI/gh-pages/metaTab.png)

![](https://raw.githubusercontent.com/Observe-secretly/HbaseGUI/gh-pages/createTab.png)


# 问题交流反馈
QQ：914245697
  
