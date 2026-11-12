(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('PaysDialogController', PaysDialogController);

    PaysDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Pays'];

    function PaysDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Pays) {
        var vm = this;

        vm.pays = entity;
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
            if (vm.pays.id !== null) {
                Pays.update(vm.pays, onSaveSuccess, onSaveError);
            } else {
                Pays.save(vm.pays, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:paysUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
