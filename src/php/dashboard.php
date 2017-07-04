<html>
  <head>
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
	<script src="https://www.google.com/jsapi"></script>

	<script>
		// 3. This function fires when Google Charts has been fully loaded
		function drawChart() {

		  // 4. Retrieve the raw JSON data
		  var jsonData = $.ajax({
			url: 'fetch_content.php?readingTime=now%20-12%20hours',
			dataType: 'json',
		  }).done(function (results) {

			// 5. Create a new DataTable (Charts expects data in this format)
			var data = new google.visualization.DataTable();

			// 6. Add two columns to the DataTable
			data.addColumn('datetime', 'Time');
			data.addColumn('number',   'Temperature');
			data.addColumn('number',   'Light');
			data.addColumn('number',   'Moisture');

			// 7. Cycle through the records, adding one row per record
			results.forEach(function(row) {
			  data.addRow([
				  (new Date(row.readingTime)),
				  parseFloat(row.temperature),
				  parseFloat(row.light),
				  parseFloat(row.moisture),
				]);
			});

			// 8. Create a new line chart
			var chart = new google.visualization.LineChart($('#chart').get(0));

			// 9. Render the chart, passing in our DataTable and any config data
			chart.draw(data, {
			  title:  'Garden Readings',
			  height: 250
			});

		  });

		}

		// 1. Start loading Google Charts
		google.load('visualization', '1', {
		  packages: ['corechart']
		});

		// 2. Set a callback function to fire when loading is complete
		google.setOnLoadCallback(drawChart);

		</script>    
	</script>
  </head>
  <body>
    <div id="chart" style="width: 600px; height: 400px;"></div>
  </body>
</html>