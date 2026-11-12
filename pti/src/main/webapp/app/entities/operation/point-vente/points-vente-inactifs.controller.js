(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PointsVenteInactifsController', PointsVenteInactifsController);

    PointsVenteInactifsController.$inject = ['RequestUtils','$rootScope', 'TableService', 'Utils','$scope','$state'];

    function PointsVenteInactifsController(RequestUtils,$rootScope,TableService, Utils, $scope,$state) {

        var vm = this;
        vm.save = save;
        vm.clear = clear;
        vm.format = null;
        vm.type = null;
        vm.back = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.back = function () {
            $state.go('etat-divers');
        };

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
        function openCalendar(date) {
            console.log("open",vm.item+'.'+date);
            vm.datePickerOpenStatus[date] = true;
        }


        function save() {
            vm.isSaving = true;
            vm.item.format = vm.item.reportFormat;
            if (vm.item.reportFormat === "PDF") {
                Utils.postPrintUrl("api/etat/export/liste-points-vente-inactifs", vm.item, "points_de_vente_inactifs_sur_30_jours.pdf");
            } else {
                Utils.downloadFile("api/etat/export/liste-points-vente-inactifs", vm.item,"points_de_vente_inactifs_sur_30_jours.xlsx");
            }
            vm.isSaving = false;
        }
    }

})();
