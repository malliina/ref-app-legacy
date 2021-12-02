create table persons(
    id int primary key auto_increment,
    name varchar(128) not null,
    age int not null
) charset=utf8mb4;

create table cars(
    id int primary key auto_increment,
    nickname varchar(128) not null,
    owner int not null,
    foreign key (owner) references persons (id)
        on update cascade on delete cascade
) charset=utf8mb4;
