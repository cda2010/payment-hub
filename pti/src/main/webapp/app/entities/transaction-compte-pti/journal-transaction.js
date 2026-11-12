(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('JournalTransactionController', JournalTransactionController);

    JournalTransactionController.$inject = ['RequestUtils','$rootScope','$state', 'TableService', 'Utils','$scope'];

    function JournalTransactionController(RequestUtils,$rootScope,$state,TableService, Utils, $scope) {

        var vm = this;
        vm.save = save;
        vm.clear = clear;
        vm.back = clear;
        vm.format = null;
        vm.type = null;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;

        vm.item = {
            format : null,
            reportFormat : "PDF",
            compteCloture : false,
            compteBloque : false,
            numTel : null,
            numCompte : null,
            debut : null,
            fin : null,
        };

        vm.hasError = hasError;

        function hasError(form, field) {
            return (form.$submitted || field.$touched) && field.$invalid;
        }

        function openCalendar(date) {
            console.log("open",vm.item+'.'+date);
            vm.datePickerOpenStatus[date] = true;
        }

        function clear() {
            vm.item = {
                compteCloture : false,
                compteBloque : false,
                reportFormat : "PDF",
                numTel : null,
                numCompte : null,
                debut : null,
                fin : null,
            };
        }

        function save() {
            vm.isSaving = true;
            vm.item.format = vm.item.reportFormat;
            if (vm.item.reportFormat === "PDF") {
                Utils.postPrintUrl("api/etat/export/journal-transaction", vm.item, "journal_transaction.pdf");
            } else {
                Utils.downloadFile("api/etat/export/journal-transaction", vm.item,"journal_transaction.xlsx");
            }
            vm.isSaving = false;
        }
        vm.back = function () {
            $state.go('etat-divers');
        }
    }

})();
