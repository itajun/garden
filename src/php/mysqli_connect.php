<?php
/**
 * Connection to the DB
 */

DEFINE ('DB_USER', 'id2138850_itajun');
DEFINE ('DB_PASSWORD', 'Welcome01');
DEFINE ('DB_HOST', 'localhost');
DEFINE ('DB_NAME', 'id2138850_iot');

$dbc = mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME)
OR die('Could not connect to MySQL: ' . mysqli_connect_error());

?>