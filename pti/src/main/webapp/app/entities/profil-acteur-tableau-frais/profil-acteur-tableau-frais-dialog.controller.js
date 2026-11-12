(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurTableauFraisDialogController', ProfilActeurTableauFraisDialogController);

    ProfilActeurTableauFraisDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ProfilActeurTableauFrais'];

    function ProfilActeurTableauFraisDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ProfilActeurTableauFrais) {
        var vm = this;

        vm.profilActeurTableauFrais = entity;
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
            if (vm.profilActeurTableauFrais.id !== null) {
                ProfilActeurTableauFrais.update(vm.profilActeurTableauFrais, onSaveSuccess, onSaveError);
            } else {
                ProfilActeurTableauFrais.save(vm.profilActeurTableauFrais, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:profilActeurTableauFraisUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
