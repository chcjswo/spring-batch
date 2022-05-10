create table person (
    id bigint primary key auto_increment,
    name varchar(255),
    age varchar(255),
    address varchar(255)
);

insert into person (name, age, address) values
('test11','12','서울1'),
('test22','22','경기1'),
('test33','32','인천1'),
('test44','42','성남1'),
('test55','52','용인1');
