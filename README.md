# AutoFuzz

**AutoFuzz** 是一款安全测试的辅助型项目，主要用于自动识别请求中的参数，根据预设的 payload 逐个发包测试，从而提高测试效率。

插件设计源于 **@smxiazi** 师傅的经典项目 **[xia_sql](https://github.com/smxiazi/xia_sql)**，根据自己理解在参数解析上进行优化，同时集成工作中常测试的越权与未授权访问场景。感谢 **@smxiazi** 的经典插件提供思路。

插件基于 `Montoya API` 开发，需要满足 BurpSuite 版本（>=2023.12.1）才能使用。

## 使用方法

### 插件安装

插件安装： `Extender - Extensions - Add - Select File - Next`

<img src="assets/image-20250303233548430.png" alt="image-20250303233548430" style="zoom:40%;" />

<img src="assets/image-20250303234145025.png" alt="image-20250303234145025" style="zoom:30%;" />

### 主要功能

#### 基本功能

- **启用插件**：顾名思义勾选后该插件启用。
- **监听 Proxy**：自动捕获经过 BurpSuite Proxy 符合条件的请求。
- **监听 Repeter**：自动捕获 BurpSuite Repeter 中符合条件的请求。
- **清空请求记录**：清空右侧表格中的记录。

> **Tips：**
>
> - 通过监听捕获的流量相同接口只会 fuzz 一次。Method + Host + Path 均相同的请求视为同一请求。
> - 通过右键菜单发送到插件获取的请求，可无视域名/IP限制，无视去重限制。

#### 域名设置

插件仅对用户设置的 **域名/IP** 相关请求发包测试，避免误伤无关站点。

- **添加域名/IP**
  - 点击左侧添加按钮即可添加域名/IP，每行一个，不可重复。

<img src="assets/image-20250304001209634.png" alt="image-20250304001209634" style="zoom:30%;" />

<img src="assets/image-20250304001418561.png" alt="image-20250304001418561" style="zoom:30%;" />

- **编辑域名/IP**
  - 选中需要编辑的条目 ，点击左侧编辑，修改后确定。修改后数据不可重复，如果选中多条，则默认修改选中第一条。

<img src="assets/image-20250304002312839.png" alt="image-20250304002312839" style="zoom:30%;" />

<img src="assets/image-20250304002640059.png" alt="image-20250304002640059" style="zoom:30%;" />

- **删除域名/IP**
  - 选中需要删除的条目，点击左侧删除，即可删除对应数据，支持多行选中删除。

<img src="assets/image-20250304003218031.png" alt="image-20250304003218031" style="zoom:30%;" />

<img src="assets/image-20250304003238900.png" alt="image-20250304003238900" style="zoom:30%;" />

- **包含子域名**
  - 以 `qq.com` 为例，如不勾选包含子域名，则 host 为 `y.qq.com` 的请求无法被捕获。

#### Payload 设置

> Tips: 当 **列表中有数据** 时，才会启用该模块功能。

- **添加payload**
  - 点击左侧添加按钮即可添加 payload，每行一个，不可重复。

<img src="assets/image-20250304211651127.png" alt="image-20250304211651127" style="zoom:30%;" />

<img src="assets/image-20250304211737496.png" alt="image-20250304211737496" style="zoom:30%;" />

- **编辑payload**
  - 选中需要编辑的条目 ，点击左侧编辑，修改后确定。修改后数据不可重复，如果选中多条，则默认修改选中第一条。

<img src="assets/image-20250304212242627.png" alt="image-20250304212242627" style="zoom:30%;" />

<img src="assets/image-20250304212334916.png" alt="image-20250304212334916" style="zoom:30%;" />

- **删除payload**
  - 选中需要删除的条目，点击左侧删除，即可删除对应数据，支持多行选中删除。

<img src="assets/image-20250304214210463.png" alt="image-20250304214210463" style="zoom:30%;" />

<img src="assets/image-20250304214245111.png" alt="image-20250304214245111" style="zoom:30%;" />

- **参数置空**
  - 勾选后可增加一项为 `空` 的 payload，在 fuzz 时会将参数值置空。

<img src="assets/image-20250304214330266.png" alt="image-20250304214330266" style="zoom:30%;" />

<img src="assets/image-20250304215106091.png" alt="image-20250304215106091" style="zoom:30%;" />

- **URL编码**
  - 勾选后，payload 中若存在特殊字符，非 json 格式的参数在 fuzz 时将对特殊字符进行 URL 编码。

<img src="assets/image-20250304215342739.png" alt="image-20250304215342739" style="zoom:30%;" />

#### Auth Header 设置

> Tips: 当 **列表中有Header数据** 或 **勾选未授权访问** 时，才会启用该模块功能。

- **添加Header**
  - 点击左侧添加按钮即可添加需要进行替换的 Header，每行一个，不可重复。

<img src="assets/image-20250304222451866.png" alt="image-20250304222451866" style="zoom:30%;" />

<img src="assets/image-20250304222531391.png" alt="image-20250304222531391" style="zoom:30%;" />

<img src="assets/image-20250304222609482.png" alt="image-20250304222609482" style="zoom:30%;" />

- **编辑Header**
  - 选中需要编辑的条目 ，点击左侧编辑，修改后确定。修改后数据不可重复，如果选中多条，则默认修改选中第一条。

<img src="assets/image-20250304222912568.png" alt="image-20250304222912568" style="zoom:30%;" />

<img src="assets/image-20250304222943732.png" alt="image-20250304222943732" style="zoom:30%;" />

- **未授权访问**
  - 勾选后，在 fuzz 时会去除列表中设置的所有 Header，进行未授权访问测试。

<img src="assets/image-20250304224510635.png" alt="image-20250304224510635" style="zoom:30%;" />

#### 查找功能

可根据设置的查找作用域在 `request` 或 `response` 中查找是否含有输入的字符串信息，不区分大小写，不支持中文查找。

<img src="assets/image-20250304231940839.png" alt="image-20250304231940839" style="zoom:30%;" />

#### 右键菜单

可通过右键菜单发送到插件，无视域名/IP范围，无视去重限制。

<img src="assets/image-20250304235348677.png" alt="image-20250304235348677" style="zoom:30%;" />

<img src="assets/image-20250304235439669.png" alt="image-20250304235439669" style="zoom:30%;" />

## FAQ

**Q1**：为什么有了 xia_sql 还重新开发 AutoFuzz，有何不同之处？

**A1**：来自朋友的需求。添加了越权相关功能测试，提高工作测试效率。xia_sql 对复杂嵌套的 json 解析并不完善，不能满足自己测试需求，进行优化后可精确替换 json 中每个参数的 value。BurpSuite 官方已经推出 Montoya API，紧跟时代，学习一下基于新 API 插件开发

**Q2**：为什么不添加判断漏洞是否存在功能？

**A2**：不想插件仅限制于测试某一种漏洞，只希望它作为辅助工具提升测试效率，主要是懒得写、不想写、写不来。

**Q3**：为什么查找功能不支持中文？

**A3**：不明原因的编码问题，暂时解决不了，有会的大佬可以帮忙解决下，感谢感谢。

**Q4**：为什么插件有报错日志，影响使用吗？

**A4**：异常处理没写好，又不太想找是哪里的异常，应该不影响使用吧，也许吧......

## 已知BUG

- 编辑时的弹窗大小未固定，导致当 payload 或 header 过长，手动编辑时弹窗的宽度不受控制。

## 致谢

公众号：卫界安全-阿呆攻防、浩凯信安