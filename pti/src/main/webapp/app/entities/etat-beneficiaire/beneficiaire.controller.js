(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BeneficiaireController', BeneficiaireController);

    BeneficiaireController.$inject = ['RequestUtils','$rootScope', 'TableService', 'Utils','Compte','DateUtils', '$scope','$state'];

    function BeneficiaireController(RequestUtils,$rootScope,TableService, Utils, Compte,DateUtils, $scope,$state) {

        var vm = this;
        vm.save = save;
        vm.clear = clear;
        vm.searchCompte = searchCompte;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.format = null;
        vm.comptes = [];
        vm.back = clear;
        vm.back = function () {
            $state.go('etat-divers');
        }


        vm.item = {
            format : null,
            reportFormat : "PDF",
            compteCloture : null,
            compteBloque : null,
            numTel : null,
            numCompte : null,
            debut : null,
            fin : null,
        };

        function searchCompte() {
           console.log("clic");
           vm.comptes = Compte.queryByTel({
               numeroTel: vm.item.numTel
           });
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
                reportFormat : "PDF",
                numTel : null,
                numCompte : null,
                debut : null,
                fin : null,
            };
        }

        function save() {
            vm.isSaving = true;

            // vm.item.dateDebut = DateUtils.convertDateTimeFromServer(vm.item.dateDebut);
            // vm.item.dateFin = DateUtils.convertDateTimeFromServer(vm.item.dateFin);
            console.log(vm.item.numTel);
            console.log(vm.item.numCompte);
            console.log(vm.item.debut);
            console.log(vm.item.fin);
            console.log(vm.item.reportFormat);

            vm.item.format = vm.item.reportFormat;
            if (vm.item.reportFormat === "PDF") {
                Utils.postPrintUrl("api/etat/export/list-beneficiaire", vm.item, "list-beneficiaire.pdf");
            } else {
                Utils.downloadFile("api/etat/export/list-beneficiaire", vm.item, "list-beneficiaire.xlsx");
            }
            vm.isSaving = false;
        }
    }

})();
