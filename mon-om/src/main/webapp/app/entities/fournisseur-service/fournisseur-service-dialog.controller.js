(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('FournisseurServiceDialogController', FournisseurServiceDialogController);

    FournisseurServiceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'FournisseurService', 'ServiceTraitement', 'Pays', 'Api'];

    function FournisseurServiceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, FournisseurService, ServiceTraitement, Pays, Api) {
        var vm = this;

        vm.fournisseurService = entity;
        vm.clear = clear;
        vm.save = save;
        vm.servicetraitements = ServiceTraitement.query();
        vm.payss = Pays.query();
        vm.apis = Api.query();
        if ( vm.fournisseurService.id!== null){
            vm.fseid=vm.fournisseurService.id;
        }else{
             vm.fseid=0;
        }
        vm.fournisseurServices=FournisseurService.queryCompense({idFse:vm.fseid});
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.fournisseurService.id !== null) {
                FournisseurService.update(vm.fournisseurService, onSaveSuccess, onSaveError);
            } else {
                FournisseurService.save(vm.fournisseurService, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('omnicanalApp:fournisseurServiceUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
