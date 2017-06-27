SELECT 1;



INSERT INTO HUMIDITY_LOG VALUES (CURRENT_TIMESTAMP, 10);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('MINUTE', -1, CURRENT_TIMESTAMP), 10);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 10);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 1);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 13);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('HOUR', -4, CURRENT_TIMESTAMP), 4);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('HOUR', -5, CURRENT_TIMESTAMP), 0);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('HOUR', -6, CURRENT_TIMESTAMP), 19);
INSERT INTO HUMIDITY_LOG VALUES (DATEADD('HOUR', -7, CURRENT_TIMESTAMP), 10);


INSERT INTO LIGHT_LOG VALUES (CURRENT_TIMESTAMP, 1);
INSERT INTO LIGHT_LOG VALUES (DATEADD('MINUTE', -1, CURRENT_TIMESTAMP), 13);
INSERT INTO LIGHT_LOG VALUES (DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 0);
INSERT INTO LIGHT_LOG VALUES (DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 6);
INSERT INTO LIGHT_LOG VALUES (DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 11);
INSERT INTO LIGHT_LOG VALUES (DATEADD('HOUR', -4, CURRENT_TIMESTAMP), 4);
INSERT INTO LIGHT_LOG VALUES (DATEADD('HOUR', -5, CURRENT_TIMESTAMP), 2);
INSERT INTO LIGHT_LOG VALUES (DATEADD('HOUR', -6, CURRENT_TIMESTAMP), 9);
INSERT INTO LIGHT_LOG VALUES (DATEADD('HOUR', -7, CURRENT_TIMESTAMP), 1);
