(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('RelationDialogController', RelationDialogController);

    RelationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Relation','FournisseurService','ServiceTraitement','TypeService'];

    function RelationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Relation,FournisseurService,ServiceTraitement,TypeService) {
        var vm = this;

        vm.relation = entity;
        vm.clear = clear;
        vm.save = save;
vm.fournisseurServices=FournisseurService.getAllFse();
vm.traitementServices =TypeService.query();


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
            $scope.$emit('omnicanalApp:relationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
