(function () {
    "use strict";
    angular.module('ptiApp').factory('DataBoxService', [DataBoxService]);
    function DataBoxService() {
        var service = {
            init:init
        };
        return service;

        function init() {
        var chartfirstcolor = "#57b5e3";
        var chartsecondcolor = "#f4b400";
        var chartthirdcolor = "#d73d32";
        var chartfourthcolor = "#8cc474";
        var chartfifthcolor = "#bc5679";
        var gridbordercolor = "#eee";

        var pieData = [
            {
                value: 30,
                color: chartfirstcolor
            },
            {
                value: 50,
                color: chartsecondcolor
            },
            {
                value: 100,
                color: chartfourthcolor
            }

        ];

        InitiateEasyPieChart.init();
        InitiateSparklineCharts.init();
        InitiateDonutChart.init();
        //Pie Chart BandWidth
        var data = [
            { data: [[1, 50]], color: '#11a9cc' },
            { data: [[1, 80]], color: '#ffce55' },
            { data: [[1, 30]], color: '#e75b8d' }
        ];
        var placeholder = $("#pie-chart-bandwidth");
        placeholder.unbind();

        $.plot(placeholder, data, {
            series: {
                pie: {
                    innerRadius: 0.7,
                    show: true,
                    gradient: {
                        radial: true,
                        colors: [
                            { opacity: 1.0 },
                            { opacity: 1.0 },
                            { opacity: 1.0 }
                        ]
                    }
                }
            }
        });

        //Pie Chart Weather
        var data = [
            { data: [[1, 30]], color: '#ffce55' },
            { data: [[1, 11]], color: '#e46f61' },
            { data: [[1, 37]], color: '#ed4e2a' },
            { data: [[1, 22]], color: '#fb6e52' }
        ];
        var placeholder = $("#pie-chart-weather");
        placeholder.unbind();

        $.plot(placeholder, data, {
            series: {
                pie: {
                    innerRadius: 0.80,
                    show: true,
                    gradient: {
                        radial: true,
                        colors: [
                            { opacity: 1.0 },
                            { opacity: 1.0 },
                            { opacity: 1.0 },
                            { opacity: 1.0 }
                        ]
                    }
                }
            }
        });
        //Bar Chart
        var d1_1 = [];
        for (var i = 1; i <= 7; i += 1)
            d1_1.push([i, parseInt(Math.random() * 50)]);

        var d1_2 = [];
        for (var i = 1; i <= 7; i += 1)
            d1_2.push([i, parseInt(Math.random() * 50)]);

        var d1_3 = [];
        for (var i = 1; i <= 7; i += 1)
            d1_3.push([i, parseInt(Math.random() * 50)]);

        var data1 = [
            {
                label: "Windows Phone",
                data: d1_1,
                bars: {
                    show: true,
                    order: 1,
                    fillColor: { colors: [{ color: chartthirdcolor }, { color: chartthirdcolor }] }
                },
                color: chartthirdcolor
            },
            {
                label: "Android",
                data: d1_2,
                bars: {
                    show: true,
                    order: 2,
                    fillColor: { colors: [{ color: chartsecondcolor }, { color: chartsecondcolor }] }
                },
                color: chartsecondcolor
            },
            {
                label: "IOS",
                data: d1_3,
                bars: {
                    show: true,
                    order: 3,
                    fillColor: { colors: [{ color: chartfirstcolor }, { color: chartfirstcolor }] }
                },
                color: chartfirstcolor
            }
        ];

        $.plot($("#bar-chart"), data1, {
            bars: {
                barWidth: 0.2,
                lineWidth: 1,
                borderWidth: 0,
                fillColor: { colors: [{ opacity: 0.6 }, { opacity: 1 }] }
            },
            xaxis: {
                ticks: [[1, 'Sun'], [2, 'Mon'], [3, 'Tues'], [4, 'Wed'], [5, 'Thu'], [6, 'Fri'], [7, 'Sat']],
                color: gridbordercolor
            },
            yaxis: {
                color: gridbordercolor
            },
            grid: {
                hoverable: true,
                clickable: false,
                borderWidth: 0,
                aboveData: false
            },
            legend: true,
            tooltip: true,
            tooltipOpts: {
                defaultTheme: false,
                content: "<b>%s</b> : <span>%x</span> : <span>%y</span>",
            }
        });

        //Real-Time Chart
        var data = [],
            totalPoints = 300;

        function getRandomData() {

            if (data.length > 0)
                data = data.slice(1);

            // Do a random walk

            while (data.length < totalPoints) {

                var prev = data.length > 0 ? data[data.length - 1] : 50,
                    y = prev + Math.random() * 10 - 5;

                if (y < 0) {
                    y = 0;
                } else if (y > 100) {
                    y = 100;
                }

                data.push(y);
            }

            // Zip the generated y values with the x values

            var res = [];
            for (var i = 0; i < data.length; ++i) {
                res.push([i, data[i]])
            }

            return res;
        }

        // Set up the control widget

        var updateInterval = 100;

        var plot = $.plot("#realtime-chart", [getRandomData()], {
            yaxis: {
                color: '#f3f3f3',
                min: 0,
                max: 100,
                tickFormatter: function (val, axis) {
                    return "";
                }
            },
            xaxis: {
                color: '#f3f3f3',
                min: 0,
                max: 100,
                tickFormatter: function (val, axis) {
                    return "";
                }
            },
            colors: ['#fff'],
            series: {
                lines: {
                    lineWidth: 2,
                    fill: false,
                    fillColor: {
                        colors: [{
                            opacity: 0.5
                        }, {
                            opacity: 0
                        }]
                    },
                    steps: false
                },
                shadowSize: 0
            },
            grid: {
                show: false,
                hoverable: true,
                clickable: false,
                borderWidth: 0,
                aboveData: false
            }
        });

        function update() {

            plot.setData([getRandomData()]);
            plot.draw();
            setTimeout(update, updateInterval);
        }
        update();
        }
    }
})();
