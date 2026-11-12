(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NationaliteDialogController', NationaliteDialogController);

    NationaliteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Nationalite', 'PersonnePhysique'];

    function NationaliteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Nationalite, PersonnePhysique) {
        var vm = this;

        vm.nationalite = entity;
        vm.clear = clear;
        vm.save = save;
        vm.personnephysiques = PersonnePhysique.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.nationalite.id !== null) {
                Nationalite.update(vm.nationalite, onSaveSuccess, onSaveError);
            } else {
                Nationalite.save(vm.nationalite, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:nationaliteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
