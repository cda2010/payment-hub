(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UoDeviseDialogController', UoDeviseDialogController);

    UoDeviseDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'UoDevise', 'Devise', 'RequestUtils'];

    function UoDeviseDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, UoDevise, Devise, RequestUtils) {
        var vm = this;

        vm.uoDevise = entity;
        vm.clear = clear;
        vm.save = save;
        vm.devises = Devise.query();
        RequestUtils.GetOrganisationByType.get({ type: "PAYS" }, function(datas) {
            vm.uniteorganisations =datas;
        });

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.uoDevise.id !== null) {
                UoDevise.update(vm.uoDevise, onSaveSuccess, onSaveError);
            } else {
                UoDevise.save(vm.uoDevise, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:uoDeviseUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
