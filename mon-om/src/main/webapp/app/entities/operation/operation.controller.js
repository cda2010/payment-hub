(function () {
    'use strict';

    angular
            .module('omnicanalApp')
            .controller('OperationController', OperationController);

    OperationController.$inject = ['Operation', 'TypeService', 'FournisseurService'];

    function OperationController(Operation, TypeService, FournisseurService) {

        var vm = this;
        vm.typeServices = TypeService.query();
        vm.fournisseurServices = FournisseurService.query();
        vm.operations = [];
        vm.afficheLigneTransaction = afficheLigneTransaction;
        vm.currentPage = 1;
        vm.itemsPerPage = 10;
        vm.itemsPerPageOptions = [10, 25, 50, 100];
        vm.maxSize = 5;
        loadAll();
        vm.numbertimer = 20000;
        vm.bool = 0;
        vm.myTimer = null;
        vm.label = "Start Timer";
        vm.stopClickTimer = stopClickTimer;
        vm.affiche = affiche;
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.datePickerOpenStatus.copdebut = false;
        vm.datePickerOpenStatus.copfin = false;
        

        function loadAll() {
            Operation.query(function (result) {
                vm.operations = result;
                vm.searchQuery = null;
                vm.totalItems = result.length;
            });
        }
        startTimer();

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }
        function affiche() {
            vm.pnumero = null;
            vm.ptypeService = null;
            vm.pfseSrc = null;
            vm.pfseDest = null;
            vm.type = null;
            vm.pdebut = null;
            vm.pfin = null;
            if (angular.isDefined(vm.numero) && vm.numero !== null)
                vm.pnumero = vm.numero;
            if (angular.isDefined(vm.typeService) && vm.typeService !== null)
                vm.ptypeService = vm.typeService.id;
            if (angular.isDefined(vm.fseSrc) && vm.fseSrc !== null)
                vm.pfseSrc = vm.fseSrc;

            if (angular.isDefined(vm.fseDest) && vm.fseDest !== null)
                vm.pfseDest = vm.fseDest;
            if (angular.isDefined(vm.copdebut) && vm.copdebut !== null) {
                vm.pdebut = vm.copdebut;
                if (angular.isDefined(vm.copfin) && vm.copfin !== null) {
                    vm.pfin = vm.copfin;
                }
            }

            Operation.operationFiltered({
                pnumero : vm.pnumero,
                ptypeServiceId : vm.ptypeService,
                pfseSrc : vm.pfseSrc,
                pfseDest : vm.pfseDest,
                pdebut : vm.pdebut,
                pfin : vm.pfin
            }, function (data) {
                    vm.operations = data;
                    vm.totalItems = data.length;
                    vm.currentPage = 1;
               },
               function () {}
            );
        }


        function startTimer() {
            if (vm.numbertimer > 0) {
                vm.myTimer = setInterval(function () {
                //    loadAll();

                }, vm.numbertimer);
                vm.label = "Stop Timer";
                vm.bool = 1;

            }

        }


        function stopTimer() {
            if (vm.myTimer) {
                clearInterval(vm.myTimer);
                vm.bool = 0;
            }
        }

        function stopClickTimer() {
            if (vm.bool === 0) {
                vm.label = "Stop Timer";
                startTimer();
            } else {
                stopTimer();
                vm.label = "Start Timer";
            }

        }
        function afficheLigneTransaction(operation) {

            vm.defOperation=operation;
            Operation.queryLigne({id: operation.id}, function (result) {
                vm.trans = result;
                console.log(result);

                // vm.numtransaction = operation.id + "-" + result.operation.codeOperation;


            }


            );
        }

        vm.updateItemsPerPage = function() {
            vm.currentPage = 1;
        };

        // function loadAll() {
        //     Operation.query({
        //         page: pagingParams.page - 1,
        //         size: vm.itemsPerPage,
        //         sort: sort()
        //     }, onSuccess, onError);
        //
        //     function sort() {
        //         var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        //         if (vm.predicate !== 'id') {
        //             result.push('id');
        //         }
        //         return result;
        //     }
        //
        //     function onSuccess(data, headers) {
        //         vm.links = ParseLinks.parse(headers('link'));
        //         vm.totalItems = headers('X-Total-Count');
        //         vm.queryCount = vm.totalItems;
        //         vm.operations = data;
        //         vm.page = pagingParams.page;
        //     }
        //
        //     function onError(error) {
        //         AlertService.error(error.data.message);
        //     }
        // }
    }
})();
