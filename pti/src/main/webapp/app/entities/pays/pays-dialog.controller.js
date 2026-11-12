(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PaysDialogController', PaysDialogController);

    PaysDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Pays', 'Operation', 'FournisseurService', 'Devise'];

    function PaysDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Pays, Operation, FournisseurService, Devise) {
        var vm = this;

        vm.pays = entity;
        vm.clear = clear;
        vm.save = save;
        vm.operations = Operation.query();
        vm.fournisseurservices = FournisseurService.query();
        vm.devises = Devise.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.pays.id !== null) {
                Pays.update(vm.pays, onSaveSuccess, onSaveError);
            } else {
                Pays.save(vm.pays, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:paysUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
