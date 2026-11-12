(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClassificationFacturierDialogController', ClassificationFacturierDialogController);

    ClassificationFacturierDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ClassificationFacturier','UniteOrganisation'];

    function ClassificationFacturierDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ClassificationFacturier,UniteOrganisation) {
        var vm = this;

        vm.classificationFacturier = entity;
        vm.clear = clear;
        vm.save = save;
        vm.uoFSE=UniteOrganisation.queryByTypeUoFSE();
        vm.classificationFacturiers=[];
        vm. initClassifications= initClassifications;

        if(vm.classificationFacturier.id){
            vm.initClassifications(vm.classificationFacturier.uniteOrganisation.id);
        }
        function initClassifications(idUo){
            vm.classificationFacturiers=[];
            ClassificationFacturier.queryByUo({id: idUo},function (result) {
                var i=0;
                for(i=0;i<result.length;i++){
                    if(result[i].id != vm.classificationFacturier.id){
                        vm.classificationFacturiers.push(result[i]);
                    }
                }
                console.log("result",vm.classificationFacturiers);
            });
        }

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            console.log(vm.classificationFacturier);
            vm.isSaving = true;
            /*if(vm.classificationFacturier.niveauSuperieur.id==null){
                vm.classificationFacturier.niveauSuperieur=null;
            }*/
            if (vm.classificationFacturier.id !== null) {
                ClassificationFacturier.update(vm.classificationFacturier, onSaveSuccess, onSaveError);
            } else {
                ClassificationFacturier.save(vm.classificationFacturier, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:classificationFacturierUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
