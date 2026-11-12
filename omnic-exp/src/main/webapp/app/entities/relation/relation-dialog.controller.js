(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('RelationDialogController', RelationDialogController);

    RelationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Relation','FournisseurService','ServiceTraitement'];

    function RelationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Relation,FournisseurService,ServiceTraitement) {
        var vm = this;

        vm.relation = entity;
        vm.clear = clear;
        vm.save = save;
vm.fournisseurServices=FournisseurService.query();
vm.traitementServices =ServiceTraitement.query();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.relation.id !== null) {
                Relation.update(vm.relation, onSaveSuccess, onSaveError);
            } else {
                Relation.save(vm.relation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:relationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
