(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProduitCanalDialogController', ProduitCanalDialogController);

    ProduitCanalDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'ProduitCanal', 'Canal', 'Produit'];

    function ProduitCanalDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, ProduitCanal, Canal, Produit) {
        var vm = this;

        vm.produitCanal = entity;
        vm.clear = clear;
        vm.save = save;
        vm.canals = Canal.query();
        vm.produits = Produit.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.produitCanal.id !== null) {
                ProduitCanal.update(vm.produitCanal, onSaveSuccess, onSaveError);
            } else {
                ProduitCanal.save(vm.produitCanal, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:produitCanalUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
