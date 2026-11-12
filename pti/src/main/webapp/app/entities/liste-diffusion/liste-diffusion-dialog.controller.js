(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeDiffusionDialogController', ListeDiffusionDialogController);

    ListeDiffusionDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ListeDiffusion', 'DetailListeDiffusion'];

    function ListeDiffusionDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ListeDiffusion, DetailListeDiffusion) {
        var vm = this;

        vm.listeDiffusion = entity;
        vm.clear = clear;
        vm.save = save;
        vm.addDetail=addDetail;
        vm.deleteDetail=deleteDetail;
        if(vm.listeDiffusion.id){
            vm.detaillistediffusions = DetailListeDiffusion.queryByListe({id:vm.listeDiffusion.id});
            //console.log(vm.detaillistediffusions);
        }else{
            vm.detaillistediffusions=[];
        }
        function addDetail(){
            vm.detaillistediffusions.push({id:null,numero:null});
        }
        function deleteDetail(id){
            vm.detaillistediffusions.splice(id,1);
            //console.log(id)
        }

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.listeDiffusion.contacts=vm.detaillistediffusions;
            vm.isSaving = true;
            if (vm.listeDiffusion.id !== null) {
                ListeDiffusion.update(vm.listeDiffusion, onSaveSuccess, onSaveError);
            } else {
                ListeDiffusion.save(vm.listeDiffusion, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:listeDiffusionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
