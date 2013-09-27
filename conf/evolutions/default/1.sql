# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ausleihe (
  id                        bigint not null,
  von                       timestamp,
  bis                       timestamp,
  frist                     timestamp,
  kommentar                 varchar(255),
  abgeschlossen             boolean,
  item_id                   bigint,
  verleiher_id              bigint,
  leiher_id                 bigint,
  constraint pk_ausleihe primary key (id))
;

create table benutzer (
  id                        bigint not null,
  name                      varchar(255),
  vorname                   varchar(255),
  geburtsdatum              timestamp,
  bewertung                 bigint,
  email                     varchar(255),
  wohnort                   varchar(255),
  plz_id                    bigint,
  u_id                      varchar(255),
  provider_id               varchar(255),
  avatar_url                varchar(255),
  auth_method               varchar(255),
  token                     varchar(255),
  secret                    varchar(255),
  access_token              varchar(255),
  token_type                varchar(255),
  expires_in                integer,
  refresh_token             varchar(255),
  hasher                    varchar(255),
  passwort                  varchar(255),
  salt                      varchar(255),
  constraint pk_benutzer primary key (id))
;

create table bewertung (
  id                        bigint not null,
  wertung                   integer,
  bewerter_id               bigint,
  bewerteter_id             bigint,
  ausleihe_id               bigint,
  constraint pk_bewertung primary key (id))
;

create table board_notice (
  id                        bigint not null,
  typ                       integer,
  bis                       timestamp,
  kommentar                 varchar(255),
  akzeptiert_post           boolean,
  akzeptiert_uebergabe      boolean,
  akzeptiert_sonstiges      varchar(255),
  uebergabe_radius_km       float,
  aktiv                     boolean,
  nur_an_freunde            boolean,
  item_id                   bigint,
  owner_id                  bigint,
  constraint ck_board_notice_typ check (typ in (0,1)),
  constraint pk_board_notice primary key (id))
;

create table empfangene_message (
  id                        bigint not null,
  read                      boolean,
  deleted                   boolean,
  message_id                bigint,
  empfaenger_id             bigint,
  constraint pk_empfangene_message primary key (id))
;

create table item (
  id                        bigint not null,
  person_id                 bigint not null,
  titel                     varchar(255),
  beschreibung              varchar(255),
  constraint pk_item primary key (id))
;

create table message (
  id                        bigint not null,
  subject                   varchar(255),
  message                   clob,
  date_time                 timestamp,
  referred                  integer,
  sender_deleted            boolean,
  sender_id                 bigint,
  typ                       varchar(1),
  ref_id                    bigint,
  constraint ck_message_typ check (typ in ('D','N','A')),
  constraint pk_message primary key (id))
;

create table person (
  id                        bigint not null,
  benutzer_id               bigint,
  owner_id                  bigint,
  simple_name               varchar(255),
  constraint pk_person primary key (id))
;

create table plz_geokoordinate (
  id                        bigint not null,
  plz                       varchar(255),
  ort                       varchar(255),
  bundesland                varchar(255),
  land                      varchar(255),
  lat                       double,
  lon                       double,
  constraint pk_plz_geokoordinate primary key (id))
;

create table security_role (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_security_role primary key (id))
;

create table tag (
  name                      varchar(255) not null,
  desc                      varchar(255),
  constraint pk_tag primary key (name))
;

create table token_db (
  uuid                      varchar(255) not null,
  email                     varchar(255),
  creation_time             timestamp,
  expiration_time           timestamp,
  is_sign_up                boolean,
  constraint pk_token_db primary key (uuid))
;

create table user_permission (
  id                        bigint not null,
  permission_value          varchar(255),
  constraint pk_user_permission primary key (id))
;


create table benutzer_board_notice (
  benutzer_id                    bigint not null,
  board_notice_id                bigint not null,
  constraint pk_benutzer_board_notice primary key (benutzer_id, board_notice_id))
;

create table Benutzer_Freund (
  benutzer_id                    bigint not null,
  friend_id                      bigint not null,
  constraint pk_Benutzer_Freund primary key (benutzer_id, friend_id))
;

create table benutzer_security_role (
  benutzer_id                    bigint not null,
  security_role_id               bigint not null,
  constraint pk_benutzer_security_role primary key (benutzer_id, security_role_id))
;

create table benutzer_user_permission (
  benutzer_id                    bigint not null,
  user_permission_id             bigint not null,
  constraint pk_benutzer_user_permission primary key (benutzer_id, user_permission_id))
;

create table board_notice_benutzer (
  board_notice_id                bigint not null,
  benutzer_id                    bigint not null,
  constraint pk_board_notice_benutzer primary key (board_notice_id, benutzer_id))
;

create table item_tag (
  item_id                        bigint not null,
  tag_name                       varchar(255) not null,
  constraint pk_item_tag primary key (item_id, tag_name))
;

create table tag_item (
  tag_name                       varchar(255) not null,
  item_id                        bigint not null,
  constraint pk_tag_item primary key (tag_name, item_id))
;
create sequence ausleihe_seq;

create sequence benutzer_seq;

create sequence bewertung_seq;

create sequence board_notice_seq;

create sequence empfangene_message_seq;

create sequence item_seq;

create sequence message_seq;

create sequence person_seq;

create sequence plz_geokoordinate_seq;

create sequence security_role_seq;

create sequence tag_seq;

create sequence token_db_seq;

create sequence user_permission_seq;

alter table ausleihe add constraint fk_ausleihe_item_1 foreign key (item_id) references item (id) on delete restrict on update restrict;
create index ix_ausleihe_item_1 on ausleihe (item_id);
alter table ausleihe add constraint fk_ausleihe_verleiher_2 foreign key (verleiher_id) references person (id) on delete restrict on update restrict;
create index ix_ausleihe_verleiher_2 on ausleihe (verleiher_id);
alter table ausleihe add constraint fk_ausleihe_leiher_3 foreign key (leiher_id) references person (id) on delete restrict on update restrict;
create index ix_ausleihe_leiher_3 on ausleihe (leiher_id);
alter table benutzer add constraint fk_benutzer_plz_4 foreign key (plz_id) references plz_geokoordinate (id) on delete restrict on update restrict;
create index ix_benutzer_plz_4 on benutzer (plz_id);
alter table bewertung add constraint fk_bewertung_bewerter_5 foreign key (bewerter_id) references benutzer (id) on delete restrict on update restrict;
create index ix_bewertung_bewerter_5 on bewertung (bewerter_id);
alter table bewertung add constraint fk_bewertung_bewerteter_6 foreign key (bewerteter_id) references benutzer (id) on delete restrict on update restrict;
create index ix_bewertung_bewerteter_6 on bewertung (bewerteter_id);
alter table bewertung add constraint fk_bewertung_ausleihe_7 foreign key (ausleihe_id) references ausleihe (id) on delete restrict on update restrict;
create index ix_bewertung_ausleihe_7 on bewertung (ausleihe_id);
alter table board_notice add constraint fk_board_notice_item_8 foreign key (item_id) references item (id) on delete restrict on update restrict;
create index ix_board_notice_item_8 on board_notice (item_id);
alter table board_notice add constraint fk_board_notice_owner_9 foreign key (owner_id) references benutzer (id) on delete restrict on update restrict;
create index ix_board_notice_owner_9 on board_notice (owner_id);
alter table empfangene_message add constraint fk_empfangene_message_message_10 foreign key (message_id) references message (id) on delete restrict on update restrict;
create index ix_empfangene_message_message_10 on empfangene_message (message_id);
alter table empfangene_message add constraint fk_empfangene_message_empfaen_11 foreign key (empfaenger_id) references benutzer (id) on delete restrict on update restrict;
create index ix_empfangene_message_empfaen_11 on empfangene_message (empfaenger_id);
alter table item add constraint fk_item_person_12 foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_item_person_12 on item (person_id);
alter table message add constraint fk_message_sender_13 foreign key (sender_id) references benutzer (id) on delete restrict on update restrict;
create index ix_message_sender_13 on message (sender_id);
alter table person add constraint fk_person_benutzer_14 foreign key (benutzer_id) references benutzer (id) on delete restrict on update restrict;
create index ix_person_benutzer_14 on person (benutzer_id);
alter table person add constraint fk_person_owner_15 foreign key (owner_id) references benutzer (id) on delete restrict on update restrict;
create index ix_person_owner_15 on person (owner_id);



alter table benutzer_board_notice add constraint fk_benutzer_board_notice_benu_01 foreign key (benutzer_id) references benutzer (id) on delete restrict on update restrict;

alter table benutzer_board_notice add constraint fk_benutzer_board_notice_boar_02 foreign key (board_notice_id) references board_notice (id) on delete restrict on update restrict;

alter table Benutzer_Freund add constraint fk_Benutzer_Freund_benutzer_01 foreign key (benutzer_id) references benutzer (id) on delete restrict on update restrict;

alter table Benutzer_Freund add constraint fk_Benutzer_Freund_benutzer_02 foreign key (friend_id) references benutzer (id) on delete restrict on update restrict;

alter table benutzer_security_role add constraint fk_benutzer_security_role_ben_01 foreign key (benutzer_id) references benutzer (id) on delete restrict on update restrict;

alter table benutzer_security_role add constraint fk_benutzer_security_role_sec_02 foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;

alter table benutzer_user_permission add constraint fk_benutzer_user_permission_b_01 foreign key (benutzer_id) references benutzer (id) on delete restrict on update restrict;

alter table benutzer_user_permission add constraint fk_benutzer_user_permission_u_02 foreign key (user_permission_id) references user_permission (id) on delete restrict on update restrict;

alter table board_notice_benutzer add constraint fk_board_notice_benutzer_boar_01 foreign key (board_notice_id) references board_notice (id) on delete restrict on update restrict;

alter table board_notice_benutzer add constraint fk_board_notice_benutzer_benu_02 foreign key (benutzer_id) references benutzer (id) on delete restrict on update restrict;

alter table item_tag add constraint fk_item_tag_item_01 foreign key (item_id) references item (id) on delete restrict on update restrict;

alter table item_tag add constraint fk_item_tag_tag_02 foreign key (tag_name) references tag (name) on delete restrict on update restrict;

alter table tag_item add constraint fk_tag_item_tag_01 foreign key (tag_name) references tag (name) on delete restrict on update restrict;

alter table tag_item add constraint fk_tag_item_item_02 foreign key (item_id) references item (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ausleihe;

drop table if exists benutzer;

drop table if exists benutzer_board_notice;

drop table if exists Benutzer_Freund;

drop table if exists benutzer_security_role;

drop table if exists benutzer_user_permission;

drop table if exists bewertung;

drop table if exists board_notice;

drop table if exists board_notice_benutzer;

drop table if exists empfangene_message;

drop table if exists item;

drop table if exists item_tag;

drop table if exists message;

drop table if exists person;

drop table if exists plz_geokoordinate;

drop table if exists security_role;

drop table if exists tag;

drop table if exists tag_item;

drop table if exists token_db;

drop table if exists user_permission;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ausleihe_seq;

drop sequence if exists benutzer_seq;

drop sequence if exists bewertung_seq;

drop sequence if exists board_notice_seq;

drop sequence if exists empfangene_message_seq;

drop sequence if exists item_seq;

drop sequence if exists message_seq;

drop sequence if exists person_seq;

drop sequence if exists plz_geokoordinate_seq;

drop sequence if exists security_role_seq;

drop sequence if exists tag_seq;

drop sequence if exists token_db_seq;

drop sequence if exists user_permission_seq;

