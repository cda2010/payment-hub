(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserListController', UserListController);

    UserListController.$inject = ['RequestUtils','$rootScope', 'TableService', 'Utils','$scope','$state'];

    function UserListController(RequestUtils,$rootScope,TableService, Utils, $scope,$state) {

        var vm = this;
        vm.save = save;
        vm.clear = clear;
        vm.format = null;
        vm.type = null;
        vm.back = clear;
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

        function save() {
            vm.isSaving = true;
            vm.item.format = vm.item.reportFormat;
            if (vm.item.format === "PDF") {
                Utils.postPrintUrl("api/etat/export/list-user", vm.item, "list-user.pdf");
            } else {
                Utils.downloadFile("api/etat/export/list-user", vm.item,"list-user.xlsx");
            }
            vm.isSaving = false;
        }
    }

})();
