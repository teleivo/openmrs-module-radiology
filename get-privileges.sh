#!/bin/bash

mysqldump -h 127.0.0.1 -uopenmrs -popenmrs openmrs role_privilege --where "role='Radiology: Referring physician'" -t > privileges.sql

