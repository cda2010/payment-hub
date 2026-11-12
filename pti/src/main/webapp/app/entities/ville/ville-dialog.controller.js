(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VilleDialogController', VilleDialogController);

    VilleDialogController.$inject = ['RequestUtils','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Ville'];

    function VilleDialogController (RequestUtils,$timeout, $scope, $stateParams, $uibModalInstance, entity, Ville) {
        var vm = this;

        vm.ville = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        RequestUtils.GetOrganisationByType.get({ type: "PAYS" }, function(datas) {
            vm.payss = datas;
        });

        function save () {
            vm.isSaving = true;
            if (vm.ville.id !== null) {
                Ville.update(vm.ville, onSaveSuccess, onSaveError);
            } else {
                Ville.save(vm.ville, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:villeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
