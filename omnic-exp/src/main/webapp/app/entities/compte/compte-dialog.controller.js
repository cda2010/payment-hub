(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('CompteDialogController', CompteDialogController);

    CompteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Compte','Relation','TypeCompte'];

    function CompteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Compte,Relation,TypeCompte) {
        var vm = this;

        vm.compte = entity;
        vm.clear = clear;
        vm.save = save;
  vm.relations = Relation.query();
    vm.typecomptes = TypeCompte.query();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.compte.id !== null) {
                Compte.update(vm.compte, onSaveSuccess, onSaveError);
            } else {
                Compte.save(vm.compte, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:compteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
