(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComposantDialogController', ComposantDialogController);

    ComposantDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Composant', 'Champs','Bouton','Liste'];

    function ComposantDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, Composant, Champs,Bouton,Liste) {
        var vm = this;

        vm.composant = entity;
        vm.clear = clear;
        vm.save = save;
        vm.enableMultiple=enableMultiple;
        vm.enableEstStatic=enableEstStatic;
        vm.champs = Champs.query();
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function enableMultiple(bool) {
            
            if(bool) {
                vm.composant.selectMultiple = 1;
                vm.composant.liste.multiple = true;
                console.log('test',bool,vm.composant.selectMultiple);
            } else {
                vm.composant.selectMultiple = 0;
                vm.composant.liste.multiple = false;
                console.log('test',bool,vm.composant.selectMultiple);
            }
        }
        
          function enableEstStatic(bool) {
            
            if(bool) {
                   console.log('true');
                vm.composant.liste.estStatic = false;
              
            } else {
                 console.log('false');
                vm.composant.liste.estStatic = true;
               
            }
        }
        

        function save () {
            vm.isSaving = true;
            if (vm.composant.id !== null) {
                if(vm.composant.liste!=null){
                    Liste.update(vm.composant.liste,function (result) {
                        vm.composant.liste=result;
                        Composant.update(vm.composant, onSaveSuccess, onSaveError);
                    });
                }else{
                    Composant.update(vm.composant, onSaveSuccess, onSaveError);
                }

            } else {
                if(vm.composant.typeComposant=='LISTE'){
                    Liste.save(vm.composant.liste,function (result) {
                        vm.composant.bouton=null;
                        vm.composant.liste=result;
                        console.log('after save liste',result);
                        Composant.save(vm.composant, onSaveSuccess, onSaveError);
                    })
                }else {
                    if(vm.composant.typeComposant=='BOUTON'){
                        Bouton.save(vm.composant.bouton,function (result) {
                            vm.composant.liste=null;
                            vm.composant.bouton=result;
                            Composant.save(vm.composant, onSaveSuccess, onSaveError);
                        })
                    }else {
                        vm.composant.liste=null;
                        vm.composant.bouton=null;
                        Composant.save(vm.composant, onSaveSuccess, onSaveError);
                    }
                }
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:composantUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
            console.log('result',result);
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
