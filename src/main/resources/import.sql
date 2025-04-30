-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

-- Hibernate needs the sequence to be updated if IDs are manually inserted
-- The sequence name is typically entityname_seq
INSERT INTO Person(id, name) VALUES (nextval('Person_seq'), 'Alice');
INSERT INTO Person(id, name) VALUES (nextval('Person_seq'), 'Bob');
INSERT INTO Person(id, name) VALUES (nextval('Person_seq'), 'Charlie');
INSERT INTO Person(id, name) VALUES (nextval('Person_seq'), 'Alice'); -- Another Alice for FindByName test

-- Make sure the sequence is updated past the highest manually inserted ID if needed
-- SELECT setval('Person_seq', (SELECT MAX(id) FROM Person)); -- This syntax might vary slightly based on H2/DB version
-- Using nextval generally handles this correctly.
