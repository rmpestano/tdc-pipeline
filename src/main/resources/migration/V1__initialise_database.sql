DROP TABLE car if EXISTS;
drop sequence if exists hibernate_sequence;
create sequence hibernate_sequence start with 1 increment by 1;


create table car (
  id integer not null,
  model varchar(255) not null,
  name varchar(255) not null,
  price double not null,
  version integer not null,
  primary key (id)
  );