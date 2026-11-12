(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('AutreParametreDialogController', AutreParametreDialogController);

    AutreParametreDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'AutreParametre', 'FournisseurService'];

    function AutreParametreDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, AutreParametre, FournisseurService) {
        var vm = this;

        vm.autreParametre = entity;
        vm.clear = clear;
        vm.save = save;
      //  vm.fournisseurservices = FournisseurService.query();
       vm.fournisseurservices= FournisseurService.getAllFse();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.autreParametre.id !== null) {
                AutreParametre.update(vm.autreParametre, onSaveSuccess, onSaveError);
            } else {
                AutreParametre.save(vm.autreParametre, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:autreParametreUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
