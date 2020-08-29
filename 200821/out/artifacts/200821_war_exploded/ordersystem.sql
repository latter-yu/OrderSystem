drop database if exists `ordersystem`;
create database if not exists `ordersystem` character set utf8;

-- 使用数据库 use `ordersystem`;
use `ordersystem`;

drop table if exists dishes;
create table dishes (
  dishId int primary key auto_increment,
  name varchar(50),
  price int -- 以分为单位，使用 float 或 double 会存在误差（不可避免）
  -- 如果数额是 198.98，可能输出为 198.9999997
);

drop table if exists user;
create table user (
  userId int primary key auto_increment,
  name varchar(50) unique,
  password varchar(50),
  isAdmin int -- 判断是否是管理员：0 表示否，1 表示是
);

drop table if exists orderuser;
create table orderuser (
  orderId int primary key auto_increment,
  userId int, -- 与 user 表中的 userId 有关联关系
  time datetime, -- 下单时间
  isDone int, -- 1 表示订单完结，0 表示订单未完结
  foreign key(userId) references user(userId)
);

drop table if exists orderdish;
create table orderdish (
  orderId int,
  dishId int,
  -- 外键
  foreign key(orderId) references orderuser(orderId),
  foreign key(dishId) references dishes(dishId)
);