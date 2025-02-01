# 血压监测应用 (BPMonitor)

## 项目简介
这是一个基于 Android 平台的血压监测应用，支持语音输入血压值并通过图表展示血压变化趋势。用户可以方便地记录和追踪自己的血压数据，系统会自动标识异常的血压值。

## 主要功能
- 多用户管理
  - 添加/删除用户
  - 每个用户独立的血压记录
  - 长按用户卡片可删除用户

- 语音输入
  - 支持中文语音识别
  - 智能解析血压值
  - 示例语音指令："高压120，低压80"

- 数据可视化
  - 折线图展示血压趋势
  - 自动标记异常值（红色警告）
  - 显示正常血压范围参考线
  - 高压和低压使用不同颜色区分
  - 按日期排序展示数据

## 技术特点
- 使用 Room 数据库存储用户和血压数据
- 采用 MPAndroidChart 实现图表展示
- 集成 Android 原生语音识别功能
- 支持 Kotlin 协程进行异步操作
- 遵循 Material Design 设计规范

## 血压标准
- 高压（收缩压）
  - 正常范围：80-140 mmHg
  - 超出范围将标红警告

- 低压（舒张压）
  - 正常范围：60-90 mmHg
  - 超出范围将标红警告

## 系统要求
- Android 7.0 (API 24) 或更高版本
- 需要麦克风权限进行语音输入
- 建议使用支持中文语音识别的设备

## 开发环境
- Android Studio Hedgehog | 2023.1.1
- Kotlin 1.9.0
- Gradle 8.2
- JDK 17
- compileSdk 34
- targetSdk 34

## 主要依赖
- androidx.room:room-runtime:2.6.0
- androidx.room:room-ktx:2.6.0
- com.github.PhilJay:MPAndroidChart:v3.1.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.6.1
- androidx.cardview:cardview:1.0.0

## 使用说明
1. 首次打开应用，点击"添加新用户"创建用户
2. 点击用户卡片进入血压记录界面
3. 点击"语音记录血压"按钮
4. 按提示说出血压值（例如："高压120，低压80"）
5. 查看图表了解血压变化趋势
6. 长按用户卡片可删除用户及其记录

## 注意事项
- 请确保设备支持中文语音识别
- 首次使用需要授予录音权限
- 同一天的血压记录会被最新的记录覆盖
- 删除用户时会同时删除该用户的所有血压记录
