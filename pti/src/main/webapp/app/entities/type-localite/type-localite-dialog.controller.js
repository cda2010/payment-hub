(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeLocaliteDialogController', TypeLocaliteDialogController);

    TypeLocaliteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'TypeLocalite', 'RequestUtils'];

    function TypeLocaliteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, TypeLocalite, RequestUtils) {
        var vm = this;

        vm.typeLocalite = entity;
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
            if (vm.typeLocalite.id !== null) {
                TypeLocalite.update(vm.typeLocalite, onSaveSuccess, onSaveError);
            } else {
                TypeLocalite.save(vm.typeLocalite, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:typeLocaliteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
