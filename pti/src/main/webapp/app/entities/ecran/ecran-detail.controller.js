(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EcranDetailController', EcranDetailController);

    EcranDetailController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Ecran', 'Produit','Champs','$uibModal','Notification'];

    function EcranDetailController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Ecran, Produit,Champs,$uibModal,Notification) {
        var vm = this;

        vm.ecran = entity;
        vm.clear = clear;
        vm.save = save;
        vm.produits = Produit.query();
        vm.curentChamp=null;
        vm.champs=Champs.queryByEcran({id:vm.ecran.id});
        console.log('vm.champs',vm.champs);
        vm.addChamps=addChamps;
        vm.editChamps=editChamps;
        vm.deleteChamps=deleteChamps;
        function addChamps(){
            $uibModal.open({
                templateUrl: 'app/entities/champs/champs-dialog.html',
                controller: 'ChampsDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            aSaisir: null,
                            min: null,
                            max: null,
                            minLength: null,
                            maxLength: null,
                            isVisible: null,
                            pattern: null,
                            label: null,
                            ecran:vm.ecran,
                            id: null
                        };
                    }
                }
            }).result.then(function() {
                vm.champs=Champs.queryByEcran({id:vm.ecran.id});
                Notification.success("Champs ajouté avec succès");
            })
        }
        function editChamps(idChamps){
            $uibModal.open({
                templateUrl: 'app/entities/champs/champs-dialog.html',
                controller: 'ChampsDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: ['Champs', function(Champs) {
                        return Champs.get({id : idChamps}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.champs=Champs.queryByEcran({id:vm.ecran.id});
                Notification.success("Champs Modifié avec succès");
            })
        }
        function deleteChamps(idChamp){
            $uibModal.open({
                templateUrl: 'app/entities/champs/champs-delete-dialog.html',
                controller: 'ChampsDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['Champs', function(Champs) {
                        return Champs.get({id : idChamp}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.champs=Champs.queryByEcran({id:vm.ecran.id});
                Notification.success("Champs Supprimé avec succès");
            })
        }
        $timeout(function (){
            vm.champs=Champs.queryByEcran({id:vm.ecran.id});
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.ecran.id !== null) {
                Ecran.update(vm.ecran, onSaveSuccess, onSaveError);
            } else {
                Ecran.save(vm.ecran, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ecranUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

    }
})();
