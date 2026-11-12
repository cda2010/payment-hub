(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChampsDialogController', ChampsDialogController);

    ChampsDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Champs', 'Composant','Ecran'];

    function ChampsDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Champs, Composant,Ecran) {
        var vm = this;

        vm.champs = entity;
        vm.clear = clear;
        vm.save = save;
        vm.ecrans=Ecran.query();
        vm.selectedComposant=selectedComposant;
        vm.isData=false;
        vm.composants = Composant.findAllListe();
        console.log('all composant',vm.composants);
        if(vm.champs.aSaisir==null){
            vm.champs.aSaisir=1;
        }
        if(vm.champs.isVisible==null){
            vm.champs.isVisible=1;
        }
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function selectedComposant(composant) {
            console.log('----ID composant-----',composant.id);

            Composant.get({id:composant.id}, function (result) {
                console.log('---- composant-----',result);

                if(result.liste!==null){
                    console.log('75885',vm.champs.composant);
                    if(result.liste.estStatic===false){
                        console.log('rrrr',result);
                        vm.isData=true;
                    } else {
                        vm.isData=false;
                    }
                }
            })
        }

        function save () {
            vm.isSaving = true;
            if (vm.champs.id !== null) {
                console.log(vm.champs);
                Champs.update(vm.champs, onSaveSuccess, onSaveError);
            } else {
                console.log('saved',vm.champs);
                Champs.save(vm.champs, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            console.log(result);
            $scope.$emit('ptiApp:champsUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
