(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParamCanalDialogController', ParamCanalDialogController);

        ParamCanalDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity','UniteOrganisation', 'ParamCanal','TypeService','CanalApplication'];

    function ParamCanalDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity,UniteOrganisation, ParamCanal,TypeService, CanalApplication) {
        var vm = this;

        vm.paramCanal = entity;
        vm.clear = clear;     
        vm.save = save;
        vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
        vm.MARCHANDs=UniteOrganisation.queryByTypeUoMARCHAND();
        vm.typeservices=TypeService.queryAll();
        vm.canals = [];
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
        vm.changeCanal = function() {
            console.log("+++++++++++++++++++++++++++++++++++"+vm.paramCanal.typeCanal+"++++++++++++"+vm.paramCanal.application);
            CanalApplication.queryByCanal({typeCanal:vm.paramCanal.typeCanal},function (result) {
                vm.canals=result;
               // init();
                console.log(result);
            })
              
        }
        vm.calculPoids = function() {
            let poids = 0;
            if (vm.paramCanal.application) poids += 1;
            if (vm.paramCanal.typeService) poids += 2;
            if (vm.paramCanal.uniteOrganisation) poids += 3;
            if (vm.paramCanal.canalApplication) poids += 4;
            vm.paramCanal.poids = poids;
        };
       //  $scope.$watch('vm.paramCanal.application', vm.calculPoids);
       // $scope.$watch('vm.paramCanal.typeService', vm.calculPoids);
       // $scope.$watch('vm.paramCanal.uniteOrganisation', vm.calculPoids);
       // $scope.$watch('vm.paramCanal.canal', vm.calculPoids);
        function save () {
            vm.isSaving = true;
            if (vm.paramCanal.id !== null) {
                console.log("++++++++++222222+++++"+vm.paramCanal.canalApplication)
                ParamCanal.update(vm.paramCanal, onSaveSuccess, onSaveError);
            } else {
               
                ParamCanal.save(vm.paramCanal, onSaveSuccess, onSaveError);
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
