(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ServiceDialogController', ServiceDialogController);

    ServiceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Service', 'FournisseurService', 'InterfaceUtilisateur', 'Methode', 'TypeService'];

    function ServiceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Service, FournisseurService, InterfaceUtilisateur, Methode, TypeService) {
        var vm = this;

        vm.service = entity;
        vm.clear = clear;
        vm.save = save;
        vm.fournisseurservices = FournisseurService.query();
        vm.interfaceutilisateurs = InterfaceUtilisateur.query();
        vm.methodes = Methode.query();
        vm.typeservices = TypeService.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.service.id !== null) {
                Service.update(vm.service, onSaveSuccess, onSaveError);
            } else {
                Service.save(vm.service, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalPresentationApp:serviceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
