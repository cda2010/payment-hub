(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$scope', 'Principal', '$interval', 'LoginService', '$state', 'Operation'];

    function HomeController($scope, Principal, $interval, LoginService, $state, Operation) {
        var vm = this;
        vm.operations = null;
        vm.nombrePerFonction = null;
        vm.account = null;
        vm.isAuthenticated = null;
        vm.histAppels = null;
        vm.AllHistAppels = null;
        vm.toDayHistAppels = null;
        vm.dataFiltere = null;
        vm.seriesData = [];
        vm.login = LoginService.open;
        vm.register = register;
        vm.codeFse = null;
        vm.uniqueFseCodes = [];
        vm.selectedDateTime = new Date();
        vm.selectedDateTimeEnd = new Date();
        vm.filtere = null;
        vm.filteredHistAppels = [];
        vm.selectedApi = null;
        vm.availableFunctions = [];
        vm.selectedFunction = null;

        vm.currentPage = 1;
        vm.itemsPerPage = 10; // Nombre d'éléments par page
        vm.itemsPerPageOptions = [5, 10, 25, 50, 100];
        vm.maxSize = 5; // Nombre maximum de pages affichées dans la pagination

        vm.filters = {
            dateStart: null, // Date de début
            dateEnd: null,   // Date de fin
            dateRequest: '',
            nomFonction: '',
            nomApi: '',
            codeFse: '',
            codeReponse: '',
            msg: '',
            duree: '',
            circuitBreaker: ''
            
        };
    
        // Tri
        vm.sortKey = '';
        vm.reverse = false;
        vm.dateError = '';

        
        // Fonction pour trier les données
        vm.sortBy = function(key) {
            if (vm.sortKey === key) {
                vm.reverse = !vm.reverse;
            } else {
                vm.sortKey = key;
                vm.reverse = false;
            }
        };
    
        // Fonction pour appliquer le tri
        vm.applySort = function(data) {
            if (vm.sortKey) {
                return data.sort((a, b) => {
                    if (a[vm.sortKey] < b[vm.sortKey]) return vm.reverse ? 1 : -1;
                    if (a[vm.sortKey] > b[vm.sortKey]) return vm.reverse ? -1 : 1;
                    return 0;
                });
            }
            return data;
        };
    
        // Fonction pour obtenir les éléments de la page courante
        vm.getPaginatedData = function() {
            if (!vm.filteredHistAppels) return [];

            var begin = (vm.currentPage - 1) * vm.itemsPerPage;
            var end = begin + vm.itemsPerPage;
            return vm.filteredHistAppels.slice(begin, end);
        };

       // vm.itemsPerPage = localStorage.getItem('itemsPerPage') || 10;

       vm.applyFilters = function() {
        // Valider les dates
        if (vm.filters.dateStart && vm.filters.dateEnd && new Date(vm.filters.dateStart) > new Date(vm.filters.dateEnd)) {
            vm.dateError = "La date de début doit être antérieure ou égale à la date de fin.";
            return;
        }
        vm.dateError = '';

        // Filtrer les données
        vm.filteredHistAppels = vm.AllHistAppels.filter(function(item) {
            var dateRequest = new Date(item.dateRequest);
            
            // Vérifier si la date est dans l'intervalle
            var isAfterStart = vm.filters.dateStart ? dateRequest >= new Date(vm.filters.dateStart) : true;
            var isBeforeEnd = vm.filters.dateEnd ? dateRequest <= new Date(vm.filters.dateEnd) : true;
            
            // Appliquer les autres filtres
            var matchesFonction = !vm.filters.nomFonction || item.nomFonction.toLowerCase().includes(vm.filters.nomFonction.toLowerCase());
            var matchesApi = !vm.filters.nomApi || item.nomApi.toLowerCase().includes(vm.filters.nomApi.toLowerCase());
            
            return isAfterStart && isBeforeEnd && matchesFonction && matchesApi;
        });

        // Réinitialiser la pagination
        vm.currentPage = 1;
    };

    
    
        // Fonction pour appliquer les filtres
        vm.applyDurationFilter = function() {
            // Exemple de filtrage
            if (vm.durationFilter === "top") {
                vm.filtere = vm.histAppels.sort((a, b) => b.duree - a.duree).slice(0, 10);
            } else if (vm.durationFilter === "bottom") {
                vm.filtere = vm.histAppels.sort((a, b) => a.duree - b.duree).slice(0, 10);
            } else {
                vm.filtere = vm.histAppels;
            }
            vm.currentPage = 1; // Réinitialiser à la première page après filtrage
        };




        $scope.$on('authenticationSuccess', function() {
            getAccount();
        });

        getAccount(); 

        function getAccount() {
            Principal.identity().then(function(account) {
                vm.account = account;
                vm.isAuthenticated = Principal.isAuthenticated;
                if (vm.account === null) {
                    $state.go('connexion');
                } else {
                    vm.selectedDateTime = new Date();
                      vm.selectedDateTimeEnd = new Date();
  
                      // Méthode pour ouvrir le datepicker
                      vm.openDatepicker = function() {
                          vm.datepickerOpened = true;
                      };
  
                      vm.openDatepickerEnd = function() {
                        vm.datepickerOpenedEnd = true;
                    };
  
                      // Méthode pour traiter la sélection de date
                      vm.dateTimeSelectionChanged = function() {
                          console.log('Selected DateTime: ', vm.selectedDateTime);
                      };
  
                      vm.dateTimeSelectionChangedEnd = function() {
                        console.log('Selected DateTime: ', vm.selectedDateTime);
                    };
  
                      vm.dateOptions = {
                          formatYear: 'yy',
                          startingDay: 1
                      };
  
                      vm.dateOptionsEnd = {
                        formatYear: 'yy',
                        startingDay: 1
                    };
  
                      vm.format = 'dd-MMMM-yyyy';
                    initializeData();
                    vm.getUniqueFseCodes = function () {
                        var codes = vm.AllHistAppels.map(function (appel) {
                            return appel.nomApi;
                        });
                       
                        vm.uniqueFseCodes = Array.from(new Set(codes));
                        console.log("Unique Fse Code "+vm.uniqueFseCodes );
                    };
                    
                     vm.AllHistAppels.$promise.then(function(data) {
                        vm.getUniqueFseCodes();  
                    });
                }
            });
        }

        function initializeData() {
            vm.operations = Operation.operationTop10Filtered();
            vm.toDayHistAppels = Operation.queryToDayAllHist();
            vm.AllHistAppels = Operation.queryToAllHist();
            vm.AllHistAppels.$promise.then(function(data) {
                if (!Array.isArray(data)) {
                    vm.AllHistAppels = [];
                    vm.filteredHistAppels = [];
                } else {
                    vm.AllHistAppels = data;
                    vm.filteredHistAppels = data;
                    vm.updateAvailableFunctions(); 
                }
            });
            vm.histAppels = vm.toDayHistAppels;
  
            vm.toDayHistAppels.$promise.then(function(data) {
                vm.updateCharts(vm.toDayHistAppels);
               
            });

            
        }
        vm.filterData = function() {
            if (vm.selectedDateTime && vm.selectedDateTimeEnd && vm.codeFse) {
                vm.filtere = vm.AllHistAppels.filter(function(appel) {
                    var date = new Date(appel.dateRequest);
                    return appel.nomApi === vm.codeFse &&
                        date >= vm.selectedDateTime && date <= vm.selectedDateTimeEnd;
                });
                return vm.AllHistAppels.filter(function(appel) {
                    var date = new Date(appel.dateRequest);
                    return appel.nomApi === vm.codeFse &&
                        date >= vm.selectedDateTime && date <= vm.selectedDateTimeEnd;
                });
                
            } if(vm.selectedDateTime && vm.selectedDateTimeEnd && !vm.codeFse){
                vm.filtere = vm.AllHistAppels.filter(function(appel) {
                    var date = new Date(appel.dateRequest);
                    return date >= vm.selectedDateTime && date <= vm.selectedDateTimeEnd;
                });
                return vm.AllHistAppels.filter(function(appel) {
                    var date = new Date(appel.dateRequest);
                    return date >= vm.selectedDateTime && date <= vm.selectedDateTimeEnd;
                });
            } else {
                vm.filtere = vm.AllHistAppels;
                return vm.AllHistAppels;
            }
        };

        vm.updateCharts = function(data) {
            var filteredData = data || vm.filterData();
            vm.updatePieChart(filteredData);
            vm.updateColumnChart(filteredData);
            vm.updateHeatmap(filteredData);
            vm.updateBoxPlot(filteredData);
        };

        vm.updatePieChart = function(data) {
            // Grouper les données par code de réponse
            var codeReponseGroups = {};
            data.forEach(function(appel) {
                if (!codeReponseGroups[appel.codeReponse]) {
                    codeReponseGroups[appel.codeReponse] = 0;
                }
                codeReponseGroups[appel.codeReponse]++;
            });

            // Calculer les pourcentages
            var total = data.length;
            var pieData = Object.keys(codeReponseGroups).map(function(code) {
                return {
                    name: 'Code ' + code,
                    y: (codeReponseGroups[code] / total) * 100
                };
            });

            // le graphique
            Highcharts.chart('vm.containerCircuitBreaker', {
                chart: {
                    type: 'pie'
                },
                title: {
                    text: 'Répartition des codes de réponse'
                },
                tooltip: {
                    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                },
                plotOptions: {
                    pie: {
                        allowPointSelect: true,
                        cursor: 'pointer',
                        dataLabels: {
                            enabled: true,
                            format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                        }
                    }
                },
                series: [{
                    name: 'Codes de réponse',
                    colorByPoint: true,
                    data: pieData
                }]
            });
        };

        vm.updateColumnChart = function(data) {
            var groupedData = {};
            data.forEach(function(operation) {
                var nomApi = operation.nomApi;
                if (!groupedData[nomApi]) {
                    groupedData[nomApi] = {
                        total: 0,
                        circuitBreakerTrue: 0,
                     //   circuitBreakerFalse: 0
                    };
                }
                groupedData[nomApi].total++;
                if (operation.circuitBreaker) {
                    groupedData[nomApi].circuitBreakerTrue++;
                }
                // else {
                 //   groupedData[nomApi].circuitBreakerFalse++;
              //  }
            });

            var categories = Object.keys(groupedData);
            var seriesTotalAppels = [];
            var seriesCircuitBreakerTrue = [];
            var seriesCircuitBreakerFalse = [];

            categories.forEach(function(nomApi) {
                seriesTotalAppels.push(groupedData[nomApi].total);
                seriesCircuitBreakerTrue.push(groupedData[nomApi].circuitBreakerTrue);
                seriesCircuitBreakerFalse.push(groupedData[nomApi].circuitBreakerFalse);
            });

            Highcharts.chart('vm.containerNombreAppelCodeReponse', {
                chart: {
                    type: 'column'
                },
                title: {
                    text: "Evolution Nombre d'Appels par API"
                },
                xAxis: {
                    categories: categories,
                    crosshair: true
                },
                yAxis: {
                    min: 0,
                    title: {
                        text: 'Nombre d\'Appels'
                    }
                },
                series: [
                    {
                        name: 'Nombre d\'Appels',
                        data: seriesTotalAppels
                    },
                    {
                        name: 'Circuit Breaker True',
                        data: seriesCircuitBreakerTrue
                    }
                    //,
                  //  {
                  //      name: 'Circuit Breaker False',
                 //       data: seriesCircuitBreakerFalse
                 //   }
                ]
            });
        };

        vm.updateHeatmap = function(data) {
            var heatmapData = vm.prepareHeatmapData(data);

            Highcharts.chart('vm.containerVariationDureeAppels', {
                chart: {
                    type: 'heatmap',
                    marginTop: 40,
                    marginBottom: 80,
                    plotBorderWidth: 1
                },
                title: {
                    text: 'Nombre d\'appels par API et par intervalles de 4 heures'
                },
                xAxis: {
                    categories: heatmapData.intervals,
                    title: {
                        text: 'Intervalles de temps'
                    }
                },
                yAxis: {
                    categories: heatmapData.apis,
                    title: {
                        text: 'API'
                    },
                    reversed: true
                },
                colorAxis: {
                    min: 0,
                    minColor: '#FFFFFF',
                    maxColor: Highcharts.getOptions().colors[0]
                },
                legend: {
                    align: 'right',
                    layout: 'vertical',
                    margin: 0,
                    verticalAlign: 'top',
                    y: 25,
                    symbolHeight: 280
                },
                tooltip: {
                    formatter: function() {
                        return '<b>' + this.series.yAxis.categories[this.point.y] + '</b> a eu<br><b>' +
                            this.point.value + '</b> appels entre <br><b>' + this.series.xAxis.categories[this.point.x] + '</b>';
                    }
                },
                series: [{
                    name: 'Nombre d\'appels',
                    borderWidth: 1,
                    data: heatmapData.data,
                    dataLabels: {
                        enabled: true,
                        color: '#000000'
                    }
                }]
            });
        };

        vm.updateBoxPlot = function(filteredData) {
            var boxPlotData = vm.prepareBoxPlotData(filteredData);
        
            Highcharts.chart('vm.containerBoxPlot', {
                chart: {
                    type: 'boxplot'
                },
                title: {
                    text: 'Durées des Appels API'
                },
                xAxis: {
                    categories: boxPlotData.categories
                },
                yAxis: {
                    title: {
                        text: 'Durée des Appels (ms)'
                    }
                },
                series: [{
                    name: 'Durées',
                    data: boxPlotData.data
                }]
            });
        };
        
        vm.prepareHeatmapData = function(data) {
            var groupedData = {};
            var intervals = ['0h-4h', '4h-8h', '8h-12h', '12h-16h', '16h-20h', '20h-24h'];

            data.forEach(function(appel) {
                var nomApi = appel.nomApi;
                var date = new Date(appel.dateRequest);
                var hours = date.getHours();
                var interval;

                if (hours < 4) {
                    interval = '0h-4h';
                } else if (hours < 8) {
                    interval = '4h-8h';
                } else if (hours < 12) {
                    interval = '8h-12h';
                } else if (hours < 16) {
                    interval = '12h-16h';
                } else if (hours < 20) {
                    interval = '16h-20h';
                } else {
                    interval = '20h-24h';
                }

                if (!groupedData[nomApi]) {
                    groupedData[nomApi] = {};
                }
                if (!groupedData[nomApi][interval]) {
                    groupedData[nomApi][interval] = 0;
                }
                groupedData[nomApi][interval]++;
            });

            var apis = Object.keys(groupedData);
            var heatmapData = [];
            apis.forEach(function(api, apiIndex) {
                intervals.forEach(function(interval, intervalIndex) {
                    var count = groupedData[api][interval] || 0;
                    heatmapData.push([intervalIndex, apiIndex, count]);
                });
            });

            return {
                apis: apis,
                intervals: intervals,
                data: heatmapData
            };
        };

        //les données pour le box plot du jour
        vm.prepareBoxPlotData = function(data) {
            var groupedData = {};

            // Grouper les durées des appels par API
            data.forEach(function(appel) {
                var apiName = appel.nomApi;
                if (!groupedData[apiName]) {
                    groupedData[apiName] = [];
                }
                groupedData[apiName].push(appel.duree);
            });

            // Calcul des statistiques pour le boxplot
            var boxPlotData = [];
            for (var api in groupedData) {
                var durations = groupedData[api];
                durations.sort(function(a, b) { return a - b; });

                var min = durations[0];
                var max = durations[durations.length - 1];
                var median = durations[Math.floor(durations.length / 2)];
                var q1 = durations[Math.floor(durations.length / 4)];
                var q3 = durations[Math.floor(durations.length * 3 / 4)];

                boxPlotData.push([min, q1, median, q3, max]);
            }

            return {
                categories: Object.keys(groupedData),
                data: boxPlotData
            };
        };

        vm.updateData = function() {
            //
            var newAppels = Operation.queryToAllHist();

            newAppels.forEach(function(appel) {
                var apiName = appel.nomApi;
                var now = new Date();   
                var dataPoint = {
                    x: now.getTime(),
                    y: appel.duree / 1000 // Conversion de millisecondes en secondes
                };

                if (!vm.seriesData[apiName]) {
                    vm.seriesData[apiName] = [];
                }
                vm.seriesData[apiName].push(dataPoint);

                // Limiter le nombre de points affichés aux 50 dernières valeurs
                if (vm.seriesData[apiName].length > 50) {
                    vm.seriesData[apiName] = vm.seriesData[apiName].slice(vm.seriesData[apiName].length - 50);
                }
            });
        };
//        $interval(function() {
//            initializeData();
//        }, 60000); // 300000 ms = 5 minutes
        function register() {
            $state.go('register');
        }

        // Mettre à jour les fonctions disponibles en fonction du FSE sélectionné
        vm.updateAvailableFunctions = function() {
            if (!vm.AllHistAppels) return;

            var functions = [];
            if (vm.codeFse) {
                // Filtrer les fonctions pour le FSE sélectionné
                functions = vm.AllHistAppels
                    .filter(item => item.nomApi === vm.codeFse)
                    .map(item => item.nomFonction);
            } else {
                // Récupérer toutes les fonctions si aucun FSE n'est sélectionné
                functions = vm.AllHistAppels.map(item => item.nomFonction);
            }
            // Éliminer les doublons et trier
            vm.availableFunctions = Array.from(new Set(functions)).sort();
            // Réinitialiser la fonction sélectionnée si elle n'existe plus dans la nouvelle liste
            if (vm.selectedFunction && !vm.availableFunctions.includes(vm.selectedFunction)) {
                vm.selectedFunction = null;
            }
        };

        // Mettre à jour le graphique de variation
        vm.updateVariationChart = function() {
            var filteredData = vm.AllHistAppels;
            
            // Filtrer par FSE si sélectionné
            if (vm.codeFse) {
                filteredData = filteredData.filter(item => item.nomApi === vm.codeFse);
            }
            
            // Filtrer par fonction si sélectionnée
            if (vm.selectedFunction) {
                filteredData = filteredData.filter(item => item.nomFonction === vm.selectedFunction);
            }

            vm.updateHeatmap(filteredData);
        };

        // Observer les changements du FSE sélectionné
        $scope.$watch('vm.codeFse', function(newValue, oldValue) {
            if (newValue !== oldValue) {
                vm.updateAvailableFunctions();
                vm.updateVariationChart();
            }
        });

        // Observer les changements de la fonction sélectionnée
        $scope.$watch('vm.selectedFunction', function(newValue, oldValue) {
            if (newValue !== oldValue) {
                vm.updateVariationChart();
            }
        });
    }
})();
