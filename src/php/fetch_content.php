<?php
/**
 * Fetches the content from the database.
 *
 * Expected params are [readingTime, deviceName and sensorName]. However, all are optional.
 * >readingTime will accept any value valid for PHP strtotime. By default, will return last 24 hours.
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
	$allParams['readingTime'] = 'now - 1 day';
}
	
$readingTime = date("Y-m-d H:i:s", strtotime($allParams['readingTime']));

$content = json_encode($allParams);

require_once('mysqli_connect.php');

$query = "SELECT CONTENT FROM DEVICE_LOG WHERE READING_TIME >= ?";

$stmt = mysqli_prepare($dbc, $query);

mysqli_stmt_bind_param($stmt, "s",
							  $readingTime);

mysqli_execute($stmt);

mysqli_stmt_bind_result($stmt, $content);

$i = 0;

echo '[';
while($row = $stmt->fetch()){
	if ($i++ > 0) {
		echo ',';
	}
	echo $content;
}
echo ']';

mysqli_stmt_close($stmt);

mysqli_close($dbc);

?>