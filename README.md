# DBeaver 二次开发

>  由于项目国产化的需要，并且原生DBeaver备份还原功能较弱。
>
> 为此基于开源DBeaver 21.0.2 版本进行个性化开发

基于[DBeaver 21.0.2](https://github.com/dbeaver/dbeaver)版本增加了以下功能

## 一、新增功能

### 1. 内置JDBC驱动

内置了常用数据库驱动，不再需要用户手动下载JDBC驱动，直接创建连接即可使用。

### 2. SQL记录

记录当前用户执行的所有SQL，方便日后进行复查

### 3. 国产数据库支持

由于项目国产化的需要，但是国产化数据库连接工具繁多，不方便开发人员日常使用。为此，我们在DBeaver中自编写了国产化数据库插件，方便用户日常使用。当前版本DBeaver支持以下国产数据库，后续将不断支持其他国产数据库:

| 数据库             | 是否支持 |
| ------------------ | -------- |
| DM                 | ✔        |
| 人大金仓           | ✔        |
| 瀚高               | ✔        |
| Oscar (神通数据库) | ✔        |


### 4. 多用户支持

原生版本DBeaver多个用户同时使用时存在workspace污染问题，为此我们在eclipse的workspace机制基础上，为DBeaver增加了多用户支持。

### 5.  备份还原支持

原生版本DBeaver 备份还原功能较弱，我们扩展了DBeaver原生备份还原功能。现已支持以下数据库备份还原:

| 数据库     | 备份 | 还原 |
| ---------- | ---- | ---- |
| MySQL      | ✔    | ✔    |
| Oracle     | ✔    | ✔    |
| SQL server | ✔    | ✔    |
| Clickhouse | ✔    | ✔    |
| DM         | ✔    | ✔    |
| 人大金仓   | ✔    | ✔    |
| 瀚高       | ✔    | ✔    |
| 神通数据库 | ✔    | ✔    |

### 6.数据源比较、数据迁移

由于国产化的需要，开发了数据迁移功能。现在支持以下数据库进行迁移:

1. DM、Kingbase、SQLServer、Oracle、Oscar、PgSQL ==> MySQL
2. MySQL ==> ClickHouse
3. Kingbase、MySQL、Oracle、Oscar、SQLServer ==> DM
4. MySQL、Oracle、SQLServer ==> Kingbase
5. MySQL 、Oracle ==> SQLServer
6. DM、MySQL、Oscar、SQLServer ==> Oracle
7. DM、MySQL、Oracle、SQLServer ==> Oscar
8. MySQL、Oracle ==> SQLite

### 7. 安全模式

新增了安全模式，当数据库支持开启安全模式时。用户在执行Update、Delete 语句时将会提示用户，并且会将删除或者更新的数据放到数据回收站中。如果需要恢复数据时直接在数据回收站中进行操作即可。

### 8. 其他新增功能

1. 表DDL转换，支持MySQL 表DDL语句转换成其他常用数据库SQL语句，后续将继续支持其他数据库
2. 测试数据生成器，能够自动生成测试数据并插入表中

## 二、操作文档
新增功能的操作文档请看此处：

## 三、建议
如果您在使用过程中有什么问题或者建议都可在Issues中提出，当然如果您觉得这个工具对您有所帮助请给一个star。
