(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CategorieServiceDialogController', CategorieServiceDialogController);

        CategorieServiceDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity','UniteOrganisation', 'CategorieService','TypeService','CanalApplication'];

    function CategorieServiceDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity,UniteOrganisation, CategorieService,TypeService, CanalApplication) {
        var vm = this;

        vm.categorieService = entity;
        vm.clear = clear;     
        vm.save = save;
        vm.typeservices=TypeService.queryAll();
        vm.canals = [];
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
     
        function save () {
            vm.isSaving = true;
            if (vm.categorieService.id !== null) {
                console.log("+++++++Ordre Update"+vm.categorieService.id);
                CategorieService.update(vm.categorieService, onSaveSuccess, onSaveError);
            } else {
                console.log("+++++++Ordre Insert"+vm.categorieService.id);
                CategorieService.save(vm.categorieService, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:paramCanalUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

    }
})();
