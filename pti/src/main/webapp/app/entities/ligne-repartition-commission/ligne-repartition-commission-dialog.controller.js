(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneRepartitionCommissionDialogController', LigneRepartitionCommissionDialogController);

    LigneRepartitionCommissionDialogController.$inject = ['TypeUOWithoutUR','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LigneRepartitionCommission', 'TableauRepartitionCommission'];

    function LigneRepartitionCommissionDialogController (TypeUOWithoutUR,$timeout, $scope, $stateParams, $uibModalInstance, entity, LigneRepartitionCommission, TableauRepartitionCommission) {
        var vm = this;

        vm.ligneRepartitionCommission = entity;
        vm.clear = clear;
        vm.save = save;
        //vm.tableaurepartitioncommissions = TableauRepartitionCommission.query();

        TypeUOWithoutUR.optimise({}, function (datas) {
            removeItem(datas,"libelleTypeUO","FSE");
            vm.typeUos = datas;
        });

        console.log('ligne',vm.ligneRepartitionCommission);
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if(vm.ligneRepartitionCommission.typeTarif=='FIXE'){
                vm.ligneRepartitionCommission.valeur=null;
            }else {
                vm.ligneRepartitionCommission.somme=null;
            }
            if (vm.ligneRepartitionCommission.id !== null) {
                LigneRepartitionCommission.update(vm.ligneRepartitionCommission, onSaveSuccess, onSaveError);
            } else {
                LigneRepartitionCommission.save(vm.ligneRepartitionCommission, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ligneRepartitionCommissionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        //fonction pour supprimer un element d'un tableau
        var removeItem = function (object, key, value) {
            if (value == undefined)
                return;
            for (var i in object) {
                if (object[i][key] == value) {
                    object.splice(i, 1);
                }
            }
        };

    }
})();
