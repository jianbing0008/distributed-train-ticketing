drop table if exists `station`;

create table `station` (
  `id` bigint not null comment 'id',
  `name` varchar(20) not null comment '站名',
  `name_pinyin` varchar(50) not null comment '站名拼音',
  `name_py` varchar(50) not null comment '站名拼音首字母',
  `create_time` datetime(3) comment '新增时间',
  `update_time` datetime(3) comment '修改时间',
  primary key (`id`),
  unique key `name_unique` (`name`)
) engine=innodb default charset=utf8mb4 comment='车站';



drop table if exists `train`;

create table `train` (
  `id` bigint not null comment 'id',
  `code` varchar(20) not null comment '车次编号',
  `type` char(1) not null comment '车次类型|枚举[TrainTypeEnum]',
  `start` varchar(20) not null comment '始发站',
  `start_pinyin` varchar(50) not null comment '始发站拼音',
  `start_time` time not null comment '出发时间',
  `end` varchar(20) not null comment '终点站',
  `end_pinyin` varchar(50) not null comment '终点站拼音',
  `end_time` time not null comment '到站时间',
  `create_time` datetime(3) comment '新增时间',
  `update_time` datetime(3) comment '修改时间',
  primary key (`id`),
  unique key `code_unique` (`code`)
) engine=innodb default charset=utf8mb4 comment='车次';





drop table if exists `train_station`;

create table `train_station` (
    `id` bigint not null comment 'id',
    `train_code` varchar(20) not null comment '车次编号',
    `index` int not null comment '站序',
    `name` varchar(20) not null comment '站名',
    `name_pinyin` varchar(50) not null comment '站名拼音',
    `in_time` time comment '进站时间',
    `out_time` time comment '出站时间',
    `stop_time` time comment '停站时长',
    `km` decimal(8, 2) not null comment '里程（公里）|从上一站到本站的距离',
    `create_time` datetime(3) comment '新增时间',
    `update_time` datetime(3) comment '修改时间',
    primary key (`id`),
    unique key `train_code_index_unique` (`train_code`, `index`),
    unique key `train_code_name_unique` (`train_code`, `name`)
) engine=innodb default charset=utf8mb4 comment='火车车站';




drop table if exists `train_carriage`;

create table `train_carriage` (
    `id` bigint not null comment 'id',
    `train_code` varchar(20) not null comment '车次编号',
    `index` int not null comment '厢号',
    `seat_type` char(1) not null comment '座位类型|枚举[SeatTypeEnum]',
    `seat_count` int not null comment '座位数',
    `row_count` int not null comment '排数',
    `col_count` int not null comment '列数',
    `create_time` datetime(3) comment '新增时间',
    `update_time` datetime(3) comment '修改时间',
    unique key `train_code_index_unique` (`train_code`, `index`),
    primary key (`id`)
) engine=innodb default charset=utf8mb4 comment='火车车厢';




drop table if exists `train_seat`;

create table `train_seat` (
    `id` bigint not null comment 'id',
    `train_code` varchar(20) not null comment '车次编号',
    `carriage_index` int not null comment '箱序',
    `row` char(2) not null comment '排号|01, 02',
    `col` char(1) not null comment '列号|枚举[SeatColEnum]',
    `seat_type` char(1) not null comment '座位类型|枚举[SeatTypeEnum]',
    `carriage_seat_index` int not null comment '同车厢座序',
    `create_time` datetime(3) comment '新增时间',
    `update_time` datetime(3) comment '修改时间',
    primary key (`id`)
) engine=innodb default charset=utf8mb4 comment='座位';

INSERT INTO station (id, name, name_pinyin, name_py, create_time, update_time)
VALUES

    (11, '北京西', 'beijingxi', 'bjx', NOW(), NOW()),
    (12, '北京南', 'beijingnan', 'bjn', NOW(), NOW()),
    (13, '北京站', 'beijingzhan', 'bjz', NOW(), NOW()),
    (14, '上海虹桥', 'shanghaihongqiao', 'shhq', NOW(), NOW()),
    (15, '上海站', 'shanghaizhan', 'shz', NOW(), NOW()),
    (16, '上海南站', 'shanghainanzhan', 'shnz', NOW(), NOW()),
    (17, '广州南', 'guangzhounan', 'gzn', NOW(), NOW()),
    (18, '广州站', 'guangzhouzhan', 'gzz', NOW(), NOW()),
    (19, '广州东站', 'guangzhoudongzhan', 'gzd', NOW(), NOW()),
    (20, '深圳站', 'shenzhenzhan', 'szz', NOW(), NOW()),
    (21, '深圳北站', 'shenzhenbeizhan', 'szb', NOW(), NOW()),
    (22, '深圳坪山站', 'shenzhenpingshanzhan', 'szps', NOW(), NOW()),

    (23, '杭州东', 'hangzhoudong', 'hzd', NOW(), NOW()),
    (24, '杭州站', 'hangzhouzhan', 'hz', NOW(), NOW()),
    (25, '杭州南站', 'hangzhounanzhan', 'hzn', NOW(), NOW()),
    (26, '温州', 'wenzhou', 'wz', NOW(), NOW()),
    (27, '温州南', 'wenzhounan', 'wzn', NOW(), NOW()),
    (28, '嘉兴', 'jiaxing', 'jx', NOW(), NOW()),
    (29, '嘉兴南', 'jiaxingnan', 'jxn', NOW(), NOW()),
    (30, '绍兴', 'shaoxing', 'sx', NOW(), NOW()),
    (31, '绍兴北', 'shaoxingbei', 'sxb', NOW(), NOW()),
    (32, '台州', 'taizhou', 'tz', NOW(), NOW()),
    (33, '台州站', 'taizhouzhan', 'tzzhan', NOW(), NOW()),
    (34, '金华', 'jinhua', 'jh', NOW(), NOW()),
    (35, '金华南', 'jinhuanan', 'jhn', NOW(), NOW()),
    (36, '义乌', 'yiwu', 'yw', NOW(), NOW()),
    (37, '义乌站', 'yiwuzhan', 'ywzhan', NOW(), NOW()),
    (38, '湖州', 'huzhou', 'hz', NOW(), NOW()),
    (39, '湖州站', 'huzhouzhan', 'hzzhan', NOW(), NOW()),
    (40, '丽水', 'lishui', 'ls', NOW(), NOW()),
    (41, '丽水站', 'lishuizhan', 'lszhan', NOW(), NOW()),
    (42, '衢州', 'quzhou', 'qz', NOW(), NOW()),
    (43, '衢州站', 'quzhouzhan', 'qzzhan', NOW(), NOW());


-- 车站数据
INSERT INTO train_station (id, train_code, `index`, name, name_pinyin, in_time, out_time, stop_time, km, create_time, update_time)
VALUES
    -- 北京 - 上海
    (1, 'G1', 1, '北京南', 'beijingnan', '08:00:00', '08:10:00', '00:10:00', 0.00, NOW(), NOW()),
    (2, 'G1', 2, '上海虹桥', 'shanghaihongqiao', '13:00:00', '13:10:00', '00:10:00', 1318.00, NOW(), NOW()),

    -- 北京 - 广州
    (3, 'G2', 1, '北京西', 'beijingxi', '09:00:00', '09:15:00', '00:15:00', 0.00, NOW(), NOW()),
    (4, 'G2', 2, '广州南', 'guangzhounan', '15:30:00', '15:40:00', '00:10:00', 2298.00, NOW(), NOW()),

    -- 上海 - 广州
    (5, 'G3', 1, '上海虹桥', 'shanghaihongqiao', '09:30:00', '09:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (6, 'G3', 2, '广州南', 'guangzhounan', '16:00:00', '16:10:00', '00:10:00', 1490.00, NOW(), NOW()),

    -- 广州 - 深圳
    (7, 'C1', 1, '广州南', 'guangzhounan', '09:00:00', '09:10:00', '00:10:00', 0.00, NOW(), NOW()),
    (8, 'C1', 2, '深圳北', 'shenzhenbeizhan', '10:00:00', '10:10:00', '00:10:00', 102.00, NOW(), NOW()),

    -- 深圳 - 广州
    (9, 'C2', 1, '深圳北', 'shenzhenbeizhan', '11:00:00', '11:10:00', '00:10:00', 0.00, NOW(), NOW()),
    (10, 'C2', 2, '广州南', 'guangzhounan', '12:00:00', '12:10:00', '00:10:00', 102.00, NOW(), NOW()),

    -- 杭州 - 温州
    (11, 'D1', 1, '杭州东', 'hangzhoudong', '10:30:00', '10:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (12, 'D1', 2, '温州南', 'wenzhounan', '13:00:00', '13:10:00', '00:10:00', 429.00, NOW(), NOW()),

    -- 温州 - 杭州
    (13, 'D2', 1, '温州南', 'wenzhounan', '08:30:00', '08:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (14, 'D2', 2, '杭州东', 'hangzhoudong', '11:00:00', '11:10:00', '00:10:00', 429.00, NOW(), NOW()),

    -- 上海 - 杭州
    (15, 'D3', 1, '上海虹桥', 'shanghaihongqiao', '07:30:00', '07:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (16, 'D3', 2, '杭州东', 'hangzhoudong', '09:00:00', '09:10:00', '00:10:00', 159.00, NOW(), NOW()),

    -- 杭州 - 上海
    (17, 'D4', 1, '杭州东', 'hangzhoudong', '12:30:00', '12:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (18, 'D4', 2, '上海虹桥', 'shanghaihongqiao', '14:00:00', '14:10:00', '00:10:00', 159.00, NOW(), NOW()),

    -- 北京 - 深圳
    (19, 'G4', 1, '北京西', 'beijingxi', '08:30:00', '08:45:00', '00:15:00', 0.00, NOW(), NOW()),
    (20, 'G4', 2, '深圳北', 'shenzhenbeizhan', '17:00:00', '17:10:00', '00:10:00', 2400.00, NOW(), NOW()),

    -- 深圳 - 北京
    (21, 'G5', 1, '深圳北', 'shenzhenbeizhan', '09:00:00', '09:10:00', '00:10:00', 0.00, NOW(), NOW()),
    (22, 'G5', 2, '北京西', 'beijingxi', '18:30:00', '18:45:00', '00:15:00', 2400.00, NOW(), NOW()),

    -- 广州 - 杭州
    (23, 'G6', 1, '广州南', 'guangzhounan', '09:30:00', '09:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (24, 'G6', 2, '杭州东', 'hangzhoudong', '17:00:00', '17:10:00', '00:10:00', 1600.00, NOW(), NOW()),

    -- 杭州 - 广州
    (25, 'G7', 1, '杭州东', 'hangzhoudong', '08:30:00', '08:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (26, 'G7', 2, '广州南', 'guangzhounan', '16:00:00', '16:10:00', '00:10:00', 1600.00, NOW(), NOW()),

    -- 上海 - 温州
    (27, 'D5', 1, '上海虹桥', 'shanghaihongqiao', '08:00:00', '08:10:00', '00:10:00', 0.00, NOW(), NOW()),
    (28, 'D5', 2, '温州南', 'wenzhounan', '11:30:00', '11:40:00', '00:10:00', 500.00, NOW(), NOW()),

    -- 温州 - 上海
    (29, 'D6', 1, '温州南', 'wenzhounan', '13:00:00', '13:10:00', '00:10:00', 0.00, NOW(), NOW()),
    (30, 'D6', 2, '上海虹桥', 'shanghaihongqiao', '16:30:00', '16:40:00', '00:10:00', 500.00, NOW(), NOW()),

    -- 北京 - 杭州
    (31, 'G8', 1, '北京南', 'beijingnan', '09:00:00', '09:15:00', '00:15:00', 0.00, NOW(), NOW()),
    (32, 'G8', 2, '杭州东', 'hangzhoudong', '14:30:00', '14:40:00', '00:10:00', 1200.00, NOW(), NOW()),

    -- 杭州 - 北京
    (33, 'G9', 1, '杭州东', 'hangzhoudong', '10:00:00', '10:10:00', '00:10:00', 0.00, NOW(), NOW()),
    (34, 'G9', 2, '北京南', 'beijingnan', '17:30:00', '17:45:00', '00:15:00', 1200.00, NOW(), NOW()),

    -- 广州 - 上海
    (35, 'G10', 1, '广州南', 'guangzhounan', '09:30:00', '09:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (36, 'G10', 2, '上海虹桥', 'shanghaihongqiao', '17:00:00', '17:10:00', '00:10:00', 1500.00, NOW(), NOW()),

    -- 上海 - 广州
    (37, 'G11', 1, '上海虹桥', 'shanghaihongqiao', '08:30:00', '08:40:00', '00:10:00', 0.00, NOW(), NOW()),
    (38, 'G11', 2, '广州南', 'guangzhounan', '16:00:00', '16:10:00', '00:10:00', 1500.00, NOW(), NOW());

-- 车次数据
INSERT INTO train (id, code, type, start, start_pinyin, start_time, end, end_pinyin, end_time, create_time, update_time)
VALUES
    -- 北京 - 上海
    (1, 'G1', 'G', '北京南', 'beijingnan', '08:00:00', '上海虹桥', 'shanghaihongqiao', '13:00:00', NOW(), NOW()),
    -- 上海 - 广州
    (2, 'G2', 'G', '上海虹桥', 'shanghaihongqiao', '09:30:00', '广州南', 'guangzhounan', '16:00:00', NOW(), NOW()),
    -- 广州 - 深圳
    (3, 'C1', 'C', '广州南', 'guangzhounan', '09:00:00', '深圳北', 'shenzhenbeizhan', '10:00:00', NOW(), NOW()),
    -- 杭州 - 温州
    (4, 'D1', 'D', '杭州东', 'hangzhoudong', '10:30:00', '温州南', 'wenzhounan', '13:00:00', NOW(), NOW()),
    -- 北京 - 广州
    (5, 'G3', 'G', '北京西', 'beijingxi', '09:00:00', '广州南', 'guangzhounan', '15:30:00', NOW(), NOW()),
    -- 深圳 - 广州
    (6, 'C2', 'C', '深圳北', 'shenzhenbeizhan', '11:00:00', '广州南', 'guangzhounan', '12:00:00', NOW(), NOW()),
    -- 温州 - 杭州
    (7, 'D2', 'D', '温州南', 'wenzhounan', '08:30:00', '杭州东', 'hangzhoudong', '11:00:00', NOW(), NOW()),
    -- 上海 - 杭州
    (8, 'D3', 'D', '上海虹桥', 'shanghaihongqiao', '07:30:00', '杭州东', 'hangzhoudong', '09:00:00', NOW(), NOW()),
    -- 杭州 - 上海
    (9, 'D4', 'D', '杭州东', 'hangzhoudong', '12:30:00', '上海虹桥', 'shanghaihongqiao', '14:00:00', NOW(), NOW()),
    -- 北京 - 深圳
    (10, 'G4', 'G', '北京西', 'beijingxi', '08:30:00', '深圳北', 'shenzhenbeizhan', '17:00:00', NOW(), NOW()),
    -- 深圳 - 北京
    (11, 'G5', 'G', '深圳北', 'shenzhenbeizhan', '09:00:00', '北京西', 'beijingxi', '18:30:00', NOW(), NOW()),
    -- 广州 - 杭州
    (12, 'G6', 'G', '广州南', 'guangzhounan', '09:30:00', '杭州东', 'hangzhoudong', '17:00:00', NOW(), NOW()),
    -- 杭州 - 广州
    (13, 'G7', 'G', '杭州东', 'hangzhoudong', '08:30:00', '广州南', 'guangzhounan', '16:00:00', NOW(), NOW()),
    -- 上海 - 温州
    (14, 'D5', 'D', '上海虹桥', 'shanghaihongqiao', '08:00:00', '温州南', 'wenzhounan', '11:30:00', NOW(), NOW()),
    -- 温州 - 上海
    (15, 'D6', 'D', '温州南', 'wenzhounan', '13:00:00', '上海虹桥', 'shanghaihongqiao', '16:30:00', NOW(), NOW()),
    -- 北京 - 杭州
    (16, 'G8', 'G', '北京南', 'beijingnan', '09:00:00', '杭州东', 'hangzhoudong', '14:30:00', NOW(), NOW()),
    -- 杭州 - 北京
    (17, 'G9', 'G', '杭州东', 'hangzhoudong', '10:00:00', '北京南', 'beijingnan', '17:30:00', NOW(), NOW()),
    -- 广州 - 上海
    (18, 'G10', 'G', '广州南', 'guangzhounan', '09:30:00', '上海虹桥', 'shanghaihongqiao', '17:00:00', NOW(), NOW()),
    -- 上海 - 广州
    (19, 'G11', 'G', '上海虹桥', 'shanghaihongqiao', '08:30:00', '广州南', 'guangzhounan', '16:00:00', NOW(), NOW());

-- 火车车厢数据
INSERT INTO train_carriage (id, train_code, `index`, seat_type, seat_count, row_count, col_count, create_time, update_time)
VALUES
    -- G1车次车厢数据
    (1, 'G1', 1, '1', 30, 6, 5, NOW(), NOW()), -- 一等座
    (2, 'G1', 2, '2', 50, 10, 5, NOW(), NOW()),
    (3, 'G1', 3, '2', 50, 10, 5, NOW(), NOW()),
    (4, 'G1', 4, '2', 50, 10, 5, NOW(), NOW()),
    (5, 'G1', 5, '2', 50, 10, 5, NOW(), NOW()),
    (6, 'G1', 6, '2', 50, 10, 5, NOW(), NOW()),
    (7, 'G1', 7, '2', 50, 10, 5, NOW(), NOW()),
    (8, 'G1', 8, '2', 50, 10, 5, NOW(), NOW()),
    (9, 'G1', 9, '2', 50, 10, 5, NOW(), NOW()),
    (10, 'G1', 10, '2', 50, 10, 5, NOW(), NOW()),
    -- G2车次车厢数据
    (11, 'G2', 1, '1', 30, 6, 5, NOW(), NOW()), -- 一等座
    (12, 'G2', 2, '2', 50, 10, 5, NOW(), NOW()),
    (13, 'G2', 3, '2', 50, 10, 5, NOW(), NOW()),
    (14, 'G2', 4, '2', 50, 10, 5, NOW(), NOW()),
    (15, 'G2', 5, '2', 50, 10, 5, NOW(), NOW()),
    (16, 'G2', 6, '2', 50, 10, 5, NOW(), NOW()),
    (17, 'G2', 7, '2', 50, 10, 5, NOW(), NOW()),
    (18, 'G2', 8, '2', 50, 10, 5, NOW(), NOW()),
    (19, 'G2', 9, '2', 50, 10, 5, NOW(), NOW()),
    (20, 'G2', 10, '2', 50, 10, 5, NOW(), NOW()),
    -- C1车次车厢数据
    (21, 'C1', 1, '2', 40, 8, 5, NOW(), NOW()),
    (22, 'C1', 2, '2', 40, 8, 5, NOW(), NOW()),
    (23, 'C1', 3, '2', 40, 8, 5, NOW(), NOW()),
    (24, 'C1', 4, '2', 40, 8, 5, NOW(), NOW()),
    (25, 'C1', 5, '2', 40, 8, 5, NOW(), NOW()),
    (26, 'C1', 6, '2', 40, 8, 5, NOW(), NOW()),
    (27, 'C1', 7, '2', 40, 8, 5, NOW(), NOW()),
    (28, 'C1', 8, '2', 40, 8, 5, NOW(), NOW()),
    (29, 'C1', 9, '2', 40, 8, 5, NOW(), NOW()),
    (30, 'C1', 10, '2', 40, 8, 5, NOW(), NOW()),
    -- D1车次车厢数据
    (31, 'D1', 1, '2', 40, 8, 5, NOW(), NOW()),
    (32, 'D1', 2, '2', 40, 8, 5, NOW(), NOW()),
    (33, 'D1', 3, '2', 40, 8, 5, NOW(), NOW()),
    (34, 'D1', 4, '2', 40, 8, 5, NOW(), NOW()),
    (35, 'D1', 5, '2', 40, 8, 5, NOW(), NOW()),
    (36, 'D1', 6, '2', 40, 8, 5, NOW(), NOW()),
    (37, 'D1', 7, '2', 40, 8, 5, NOW(), NOW()),
    (38, 'D1', 8, '2', 40, 8, 5, NOW(), NOW()),
    (39, 'D1', 9, '2', 40, 8, 5, NOW(), NOW()),
    (40, 'D1', 10, '2', 40, 8, 5, NOW(), NOW()),
    -- G3车次车厢数据
    (41, 'G3', 1, '1', 30, 6, 5, NOW(), NOW()), -- 一等座
    (42, 'G3', 2, '2', 50, 10, 5, NOW(), NOW()),
    (43, 'G3', 3, '2', 50, 10, 5, NOW(), NOW()),
    (44, 'G3', 4, '2', 50, 10, 5, NOW(), NOW()),
    (45, 'G3', 5, '2', 50, 10, 5, NOW(), NOW()),
    (46, 'G3', 6, '2', 50, 10, 5, NOW(), NOW()),
    (47, 'G3', 7, '2', 50, 10, 5, NOW(), NOW()),
    (48, 'G3', 8, '2', 50, 10, 5, NOW(), NOW()),
    (49, 'G3', 9, '2', 50, 10, 5, NOW(), NOW()),
    (50, 'G3', 10, '2', 50, 10, 5, NOW(), NOW()),
    -- C2车次车厢数据
    (51, 'C2', 1, '2', 40, 8, 5, NOW(), NOW()),
    (52, 'C2', 2, '2', 40, 8, 5, NOW(), NOW()),
    (53, 'C2', 3, '2', 40, 8, 5, NOW(), NOW()),
    (54, 'C2', 4, '2', 40, 8, 5, NOW(), NOW()),
    (55, 'C2', 5, '2', 40, 8, 5, NOW(), NOW()),
    (56, 'C2', 6, '2', 40, 8, 5, NOW(), NOW()),
    (57, 'C2', 7, '2', 40, 8, 5, NOW(), NOW()),
    (58, 'C2', 8, '2', 40, 8, 5, NOW(), NOW()),
    (59, 'C2', 9, '2', 40, 8, 5, NOW(), NOW()),
    (60, 'C2', 10, '2', 40, 8, 5, NOW(), NOW()),
    -- D2车次车厢数据
    (61, 'D2', 1, '2', 40, 8, 5, NOW(), NOW()),
    (62, 'D2', 2, '2', 40, 8, 5, NOW(), NOW()),
    (63, 'D2', 3, '2', 40, 8, 5, NOW(), NOW()),
    (64, 'D2', 4, '2', 40, 8, 5, NOW(), NOW()),
    (65, 'D2', 5, '2', 40, 8, 5, NOW(), NOW()),
    (66, 'D2', 6, '2', 40, 8, 5, NOW(), NOW()),
    (67, 'D2', 7, '2', 40, 8, 5, NOW(), NOW()),
    (68, 'D2', 8, '2', 40, 8, 5, NOW(), NOW()),
    (69, 'D2', 9, '2', 40, 8, 5, NOW(), NOW()),
    (70, 'D2', 10, '2', 40, 8, 5, NOW(), NOW()),
    -- D3车次车厢数据
    (71, 'D3', 1, '2', 40, 8, 5, NOW(), NOW()),
    (72, 'D3', 2, '2', 40, 8, 5, NOW(), NOW()),
    (73, 'D3', 3, '2', 40, 8, 5, NOW(), NOW()),
    (74, 'D3', 4, '2', 40, 8, 5, NOW(), NOW()),
    (75, 'D3', 5, '2', 40, 8, 5, NOW(), NOW()),
    (76, 'D3', 6, '2', 40, 8, 5, NOW(), NOW()),
    (77, 'D3', 7, '2', 40, 8, 5, NOW(), NOW()),
    (78, 'D3', 8, '2', 40, 8, 5, NOW(), NOW()),
    (79, 'D3', 9, '2', 40, 8, 5, NOW(), NOW()),
    (80, 'D3', 10, '2', 40, 8, 5, NOW(), NOW()),
    -- D4车次车厢数据
    (81, 'D4', 1, '2', 40, 8, 5, NOW(), NOW()),
    (82, 'D4', 2, '2', 40, 8, 5, NOW(), NOW()),
    (83, 'D4', 3, '2', 40, 8, 5, NOW(), NOW()),
    (84, 'D4', 4, '2', 40, 8, 5, NOW(), NOW()),
    (85, 'D4', 5, '2', 40, 8, 5, NOW(), NOW()),
    (86, 'D4', 6, '2', 40, 8, 5, NOW(), NOW()),
    (87, 'D4', 7, '2', 40, 8, 5, NOW(), NOW()),
    (88, 'D4', 8, '2', 40, 8, 5, NOW(), NOW()),
    (89, 'D4', 9, '2', 40, 8, 5, NOW(), NOW()),
    (90, 'D4', 10, '2', 40, 8, 5, NOW(), NOW()),
    -- G4车次车厢数据
    (91, 'G4', 1, '1', 30, 6, 5, NOW(), NOW()), -- 一等座
    (92, 'G4', 2, '2', 50, 10, 5, NOW(), NOW()),
    (93, 'G4', 3, '2', 50, 10, 5, NOW(), NOW()),
    (94, 'G4', 4, '2', 50, 10, 5, NOW(), NOW()),
    (95, 'G4', 5, '2', 50, 10, 5, NOW(), NOW()),
    (96, 'G4', 6, '2', 50, 10, 5, NOW(), NOW()),
    (97, 'G4', 8, '2', 50, 10, 5, NOW(), NOW()),
    (98, 'G4', 9, '2', 50, 10, 5, NOW(), NOW()),
    (99, 'G4', 10, '2', 50, 10, 5, NOW(), NOW()),
    -- G5车次车厢数据
    (101, 'G5', 1, '1', 30, 6, 5, NOW(), NOW()), -- 一等座
    (102, 'G5', 2, '2', 50, 10, 5, NOW(), NOW()),
    (103, 'G5', 3, '2', 50, 10, 5, NOW(), NOW()),
    (104, 'G5', 4, '2', 50, 10, 5, NOW(), NOW()),
    (105, 'G5', 5, '2', 50, 10, 5, NOW(), NOW()),
    (106, 'G5', 6, '2', 50, 10, 5, NOW(), NOW()),
    (107, 'G5', 7, '2', 50, 10, 5, NOW(), NOW()),
    (108, 'G5', 8, '2', 50, 10, 5, NOW(), NOW()),
    (109, 'G5', 9, '2', 50, 10, 5, NOW(), NOW()),
    (110, 'G5', 10, '2', 50, 10, 5, NOW(), NOW()),
    -- G6车次车厢数据
    (111, 'G6', 1, '1', 30, 6, 5, NOW(), NOW()), -- 一等座
    (112, 'G6', 2, '2', 50, 10, 5, NOW(), NOW()),
    (113, 'G6', 3, '2', 50, 10, 5, NOW(), NOW()),
    (114, 'G6', 4, '2', 50, 10, 5, NOW(), NOW()),
    (115, 'G6', 5, '2', 50, 10, 5, NOW(), NOW()),
    (116, 'G6', 6, '2', 50, 10, 5, NOW(), NOW()),
    (117, 'G6', 7, '2', 50, 10, 5, NOW(), NOW()),
    (118, 'G6', 8, '2', 50, 10, 5, NOW(), NOW()),
    (119, 'G6', 9, '2', 50, 10, 5, NOW(), NOW()),
    (120, 'G6', 10, '2', 50, 10, 5, NOW(), NOW()),
    -- G7车次车厢数据
    (121, 'G7', 1, '1', 30, 6, 5, NOW(), NOW()), -- 一等座
    (122, 'G7', 2, '2', 50, 10, 5, NOW(), NOW()),
    (123, 'G7', 3, '2', 50, 10, 5, NOW(), NOW()),
    (124, 'G7', 4, '2', 50, 10, 5, NOW(), NOW()),
    (125, 'G7', 5, '2', 50, 10, 5, NOW(), NOW())