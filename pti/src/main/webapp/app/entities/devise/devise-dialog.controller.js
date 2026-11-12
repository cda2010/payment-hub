(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DeviseDialogController', DeviseDialogController);

    DeviseDialogController.$inject = ['$timeout','isViewing', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Devise', 'CompteBanque', 'Pays'];

    function DeviseDialogController ($timeout,isViewing, $scope, $stateParams, $uibModalInstance, entity, Devise, CompteBanque, Pays) {
        var vm = this;

        vm.devise = entity;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.save = save;
        vm.comptebanques = CompteBanque.query();
        vm.pays = Pays.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.devise.id !== null) {
                Devise.update(vm.devise, onSaveSuccess, onSaveError);
            } else {
                Devise.save(vm.devise, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:deviseUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
