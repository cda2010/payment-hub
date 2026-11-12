(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParamExecutionJobDialogController', ParamExecutionJobDialogController);

    ParamExecutionJobDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ParamExecutionJob', 'PtiJobDetails'];

    function ParamExecutionJobDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ParamExecutionJob, PtiJobDetails) {
        var vm = this;

        vm.paramExecutionJob = entity;
        vm.clear = clear;
        vm.save = save;
        vm.ptijobdetails = PtiJobDetails.query();
        console.log('data',vm.paramExecutionJob.ptiJobDetail.id);
        ParamExecutionJob.countByKeys({cle:vm.paramExecutionJob.ptiJobDetail.id},function (result) {
            console.log('nombre',result);
                if(result.cle>0){
                    console.log('nombre',result.cle);
                    var nbr=result.cle;
                    nbr=nbr+1;
                    vm.paramExecutionJob.cle="PARAMETRE"+nbr;
                } else{

                    vm.paramExecutionJob.cle="PARAMETRE1";
                }
        });
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.paramExecutionJob.id !== null) {
                ParamExecutionJob.update(vm.paramExecutionJob, onSaveSuccess, onSaveError);
            } else {
                ParamExecutionJob.save(vm.paramExecutionJob, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:paramExecutionJobUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
