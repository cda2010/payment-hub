(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeDialogController', TaxeDialogController);

    TaxeDialogController.$inject = ['RequestUtils','EtiquetteCompte','$rootScope','ChoisirUo','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Taxe'];

    function TaxeDialogController (RequestUtils,EtiquetteCompte,$rootScope,ChoisirUo,$timeout, $scope, $stateParams, $uibModalInstance, entity, Taxe) {
        var vm = this;

        vm.taxe = entity;
        vm.clear = clear;
        vm.save = save;

        vm.etiquetteComptes = RequestUtils.EtiquetteCompteList.query();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.taxe.id !== null) {
                Taxe.update(vm.taxe, onSaveSuccess, onSaveError);
            } else {
                Taxe.save(vm.taxe, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:taxeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
