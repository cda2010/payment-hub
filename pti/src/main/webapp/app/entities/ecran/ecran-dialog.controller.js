(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EcranDialogController', EcranDialogController);

    EcranDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Ecran', 'Produit','Facturier'];

    function EcranDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Ecran, Produit,Facturier) {
        var vm = this;

        vm.ecran = entity;
        vm.clear = clear;
        vm.save = save;
        vm.produits = Produit.query();
        vm.facturiers=Facturier.query();
        vm.idFacturier=0;
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
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
            //console.log(result)
            $scope.$emit('ptiApp:ecranUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

    }
})();
