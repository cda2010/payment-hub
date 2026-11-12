(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteBloqueClotureController', CompteBloqueClotureController);

    CompteBloqueClotureController.$inject = ['RequestUtils','$rootScope', 'TableService', 'Utils','$scope','$state'];

    function CompteBloqueClotureController(RequestUtils,$rootScope,TableService, Utils, $scope,$state) {

        var vm = this;
        vm.save = save;
        vm.clear = clear;
        vm.format = null;
        vm.type = null;
        vm.comptes = [];
        vm.back = clear;
        vm.back = function () {
            $state.go('etat-divers');
        }


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
            if (vm.type === "CLOTURE"){
                vm.item.compteCloture = true;
                vm.item.compteBloque = false;
            } else if (vm.type === "BLOQUE"){
                vm.item.compteBloque = true;
                vm.item.compteCloture = false;
            } else {
                vm.item.compteBloque = false;
                vm.item.compteCloture = false;
            }

            vm.item.format = vm.item.reportFormat;
            if (vm.item.reportFormat === "PDF") {
                Utils.postPrintUrl("api/etat/export/list-compte-bloque-cloture", vm.item, "list-compte-bloque-or-cloture.xlsx");
            } else {
                Utils.downloadFile("api/etat/export/list-compte-bloque-cloture", vm.item,"list-compte-bloque-or-cloture.xlsx");
            }
            vm.isSaving = false;
        }
    }

})();
