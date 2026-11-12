(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FacturierProfilDialogController', FacturierProfilDialogController);

        FacturierProfilDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'FacturierProfil', 'Facturier', 'GroupeProfilActeur'];

    function FacturierProfilDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, FacturierProfil, Facturier, GroupeProfilActeur) {
        var vm = this;

        vm.facturierProfil = entity;
        vm.clear = clear;
        vm.save = save;
        vm.facturiers=Facturier.query();
        vm.groupeProfilActeurs=GroupeProfilActeur.query();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.facturierProfil.id !== null) {
                FacturierProfil.update(vm.facturierProfil, onSaveSuccess, onSaveError);
            } else {
                //console.log("+++++++++++++++"+vm.facturierProfil.Facturier)
                FacturierProfil.save(vm.facturierProfil, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:facturierProfilUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
