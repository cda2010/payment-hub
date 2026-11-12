(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceDialogController', TypeServiceDialogController);

    TypeServiceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypeService', 'Operation', 'TableauFrais', 'TableauRepartitionCommission', 'Autorisation', 'Canal', 'EtiquetteCompte'];

    function TypeServiceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TypeService, Operation, TableauFrais, TableauRepartitionCommission, Autorisation, Canal, EtiquetteCompte) {
        var vm = this;

        vm.typeService = entity;
        vm.clear = clear;
        vm.save = save;
        //vm.operations = Operation.query();
        //vm.tableaufrais = TableauFrais.query();
        //vm.tableaurepartitioncommissions = TableauRepartitionCommission.query();
        //vm.autorisations = Autorisation.query();
        vm.canals = Canal.query();
        vm.etiquetteCode= EtiquetteCompte.queryByPTI();
        console.log(vm.etiquetteCode);
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.typeService.id !== null) {
                TypeService.update(vm.typeService, onSaveSuccess, onSaveError);
            } else {
                TypeService.save(vm.typeService, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:typeServiceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
