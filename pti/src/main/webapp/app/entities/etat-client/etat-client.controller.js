(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('EtatClientController', EtatController);

        EtatController.$inject = ['BASE_URL_API', '$scope', '$state', 'TypeService', 'RequestUtils', 'Utils'];

        function EtatController(BASE_URL_API, $scope, $state, TypeService, RequestUtils, Utils) {

            var vm = this;
            vm.datePickerOpenStatus = {};
            vm.genererRapport = genererRapport;
            vm.openCalendar = openCalendar;
            vm.format = null;
            var URL = BASE_URL_API + "etat/transaction/";
            vm.item = {
                dateDebut: null,
                dateFin: null,
                idClient: null,
                format: null
            };
            function openCalendar(date) {
                console.log("open",vm.item+'.'+date);
                vm.datePickerOpenStatus[date] = true;
            }
            function genererRapport() {
                vm.item.format = vm.format;
                if (vm.format === null || vm.format==='') {
                    vm.item.format = 'PDF';
                }
                console.log(vm.item);
                if (vm.item.format === 'PDF' || vm.format === null) {
                    Utils.postPrintUrl(URL, vm.item);
                } else {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportTransactionClient.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportTransactionClient.csv';
                    }
                    Utils.downloadFile(URL, vm.item, vm.name);
                }
            }

            vm.clear = function () {
                reset();
            };

            function reset() {
                vm.item = {
                    dateDebut: null,
                    dateFin: null,
                    idClient: null,
                    format: null
                };
                vm.format= null;
            }

        }
    }

)();
