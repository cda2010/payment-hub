(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('RestDialogController', RestDialogController);

    RestDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Rest'];

    function RestDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Rest) {
        var vm = this;

        vm.rest = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.rest.id !== null) {
                Rest.update(vm.rest, onSaveSuccess, onSaveError);
            } else {
                Rest.save(vm.rest, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:restUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
