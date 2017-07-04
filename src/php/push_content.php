<?php
/**
 * Pushes the content of the variables to the database.
 *
 * Expected params are [readingTime, deviceName and sensorName]. However, all are optional.
 * If [readingTime] is not received as a parameter, will use the current data/time.
 * All parameters, including the default ones, will be stored as "content".
 */
 
date_default_timezone_set('Australia/Sydney');

$allParams = array_merge($_GET, $_POST);

if (!isset($allParams['deviceName'])) {
	$deviceName = 'default';
	$allParams['deviceName'] = $deviceName;
} else {
	$deviceName = $allParams['deviceName'];
}

if (!isset($allParams['sensorName'])) {
	$sensorName = 'default';
	$allParams['sensorName'] = $sensorName;
} else {
	$sensorName = $allParams['sensorName'];
}

if (!isset($allParams['readingTime'])) {
	$readingTime = date("Y-m-d H:i:s", strtotime('now'));
} else {
	$readingTime = date("Y-m-d H:i:s", strtotime($allParams['readingTime']));
}

$allParams['readingTime'] = $readingTime;

$content = json_encode($allParams);

require_once('mysqli_connect.php');

$query = "INSERT INTO DEVICE_LOG(DEVICE_NAME, SENSOR_NAME, READING_TIME, CONTENT) VALUES (?, ?, ?, ?)";

$stmt = mysqli_prepare($dbc, $query);

mysqli_stmt_bind_param($stmt, "ssss",
							  $deviceName,
							  $sensorName,
							  $readingTime,
							  $content);

mysqli_stmt_execute($stmt);

$affected_rows = mysqli_stmt_affected_rows($stmt);

if ($affected_rows != 1) {
echo mysqli_error($dbc);
}
  
mysqli_stmt_close($stmt);

mysqli_close($dbc);

?>
