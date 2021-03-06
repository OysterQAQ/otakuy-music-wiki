
# otakuy-music-wiki
***

[otakuy-music-wiki](https://otakuy.com)(宅乐(yue)维基)

以音乐专辑百科信息为文章单位,人人可以参与维护修正的虚拟社区

![](https://img.shields.io/github/stars/OysterQAQ/otakuy-music-wiki.svg) ![](https://img.shields.io/github/forks/OysterQAQ/otakuy-music-wiki.svg) ![](https://img.shields.io/badge/license-AGPLv3-blue.svg)
## 简介
类似以专辑信息为文章单位的虚拟社区(论坛),用户创建好基础专辑信息经管理员审核通过后,用户将成为该专辑的维护者,其他用户提交修改由维护者审核后可以合并,将生成贡献者名单.
整体项目由社区+推荐系统组成

## Bright Point
* JDK8函数式编程风格,JDK9流的使用
* 基于reactor的异步响应式web开发
* 统一定制的异常处理(包括数据校验)
* 统一定制响应格式
* 使用lombok尽可能优雅代码
* 完善的(并不)api文档

## 技术栈
>  社区模块:
* Spring Boot
* Spring Web Reactive(WebFlux & Webclient)
* Reactive Spring Security & jjwt
* Reactive Spring Data MongoDb

* ![图片](https://wx4.sinaimg.cn/large/0069Zpkhgy1g1cotgpgbnj32s91lrwxu.jpg)

分为音乐区与讨论区

>  推荐系统:

* ![图片](https://ws4.sinaimg.cn/large/006346uDgy1fyizbk6vobj33jf2gxhdt.jpg)
